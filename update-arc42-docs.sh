
#!/usr/bin/env bash

# One-shot AI documentation updater for the arc42 docs of the PizzaProject
# subproject in this repository (monorepo: bot + project live together).
# Triggered by GitHub Webhook through the FastAPI bot (local-agent/main.py),
# or manually: bash update-arc42-docs.sh
#
# Requirements:
#   git, bash, and the AI CLI configured via AGENT_CMD (default: opencode)
#   flock (optional; a portable mkdir+pid fallback lock is used when absent)
#   gh (optional, used by the agent for automatic pull request creation)
#
# Authentication:
#   Configure GitHub CLI and git credentials on the runner beforehand.
#
# The script:
#   1. Clones this repository.
#   2. Determines the change range since the previous successful run.
#   3. Runs the AI agent with a short prompt. All rules, the workflow and the
#      git operations (branch, commit, push, PR) are defined in AGENT.md and
#      the skills inside the repository itself.
#   4. Guards that the agent did not move the remote main branch.
#   5. Saves a checkpoint so the next run starts from the current main.
#
# It processes exactly one update and then exits.

set -Eeuo pipefail

# --------------------------------------------------
# Configuration
# --------------------------------------------------

REPO_URL="${REPO_URL:-git@github.com:SanzharBerdikozhoev/LangeNachtDesCodings.git}"

MAIN_BRANCH="${MAIN_BRANCH:-main}"

DOCS_PATH="${DOCS_PATH:-PizzaProject_Tashbaev_Kurbanov_Berdikozhoev/docs/arc42}"

AGENT_CMD="${AGENT_CMD:-opencode run}"

PR_CREATE="${PR_CREATE:-true}"

DESKTOP_DIR="${XDG_DESKTOP_DIR:-}"
if [[ -z "$DESKTOP_DIR" ]] && command -v xdg-user-dir >/dev/null 2>&1; then
    DESKTOP_DIR="$(xdg-user-dir DESKTOP)"
fi
DESKTOP_DIR="${DESKTOP_DIR:-$HOME/Desktop}"
WORKDIR_ROOT="${WORKDIR_ROOT:-${DESKTOP_DIR}/arc42-doc-agent}"

STATE_DIR="${STATE_DIR:-${XDG_STATE_HOME:-$HOME/.local/state}/arc42-doc-agent}"

STATE_FILE="${STATE_FILE:-${STATE_DIR}/last-processed-main}"

LOCK_FILE="${LOCK_FILE:-${STATE_DIR}/agent.lock}"

LOCK_DIR="${LOCK_DIR:-${STATE_DIR}/agent.lock.d}"

# --------------------------------------------------
# Initialization
# --------------------------------------------------

mkdir -p "$WORKDIR_ROOT"
mkdir -p "$STATE_DIR"

log() {
    printf '[%s] %s\n' "$(date -u +%Y-%m-%dT%H:%M:%SZ)" "$*" >&2
}

fail() {
    log "ERROR: $*"
    exit 1
}

RUN_DIR=""

cleanup() {
    if [[ -d "${LOCK_DIR:-}" ]]; then
        rm -rf -- "$LOCK_DIR"
    fi
}

trap cleanup EXIT

# --------------------------------------------------
# Dependency checks
# --------------------------------------------------

for cmd in git bash mktemp; do
    command -v "$cmd" >/dev/null 2>&1 ||
        fail "Required command not found: $cmd"
done

# Check AI CLI command.
read -r -a AGENT_CMD_ARGS <<< "$AGENT_CMD"

[[ ${#AGENT_CMD_ARGS[@]} -gt 0 ]] ||
    fail "AGENT_CMD is empty."

command -v "${AGENT_CMD_ARGS[0]}" >/dev/null 2>&1 ||
    fail "AI CLI not found: ${AGENT_CMD_ARGS[0]}"

# Validate documentation path.
case "$DOCS_PATH" in
    /*|..|../*|*/../*|*/..)
        fail "DOCS_PATH must be a safe relative path."
        ;;
esac

# --------------------------------------------------
# Lock
# --------------------------------------------------

acquire_lock() {
    if command -v flock >/dev/null 2>&1; then
        exec 9>"$LOCK_FILE"
        if ! flock -n 9; then
            log "Another documentation update is already running."
            exit 0
        fi
        return
    fi

    # Fallback for systems without flock (e.g. macOS): atomic mkdir + pid check.
    if [[ -d "$LOCK_DIR" ]]; then
        local old_pid
        old_pid="$(cat "${LOCK_DIR}/pid" 2>/dev/null || true)"
        if [[ -n "$old_pid" ]] && kill -0 "$old_pid" 2>/dev/null; then
            log "Another documentation update is already running (pid ${old_pid})."
            exit 0
        fi
        log "Removing stale lock (pid ${old_pid:-unknown} is not running)."
        rm -rf -- "$LOCK_DIR"
    fi

    if ! mkdir "$LOCK_DIR" 2>/dev/null; then
        log "Another documentation update is already running."
        exit 0
    fi

    printf '%s\n' "$$" > "${LOCK_DIR}/pid"
}

acquire_lock

log "Documentation updater started."
log "Repository: $REPO_URL"
log "Main branch: $MAIN_BRANCH"
log "Documentation: $DOCS_PATH"

# --------------------------------------------------
# Clone repository
# --------------------------------------------------

RUN_DIR="$(mktemp -d "${WORKDIR_ROOT}/run.XXXXXX")"

REPO_DIR="${RUN_DIR}/repo"

log "Cloning repository..."

git clone --quiet "$REPO_URL" "$REPO_DIR" ||
    fail "Could not clone repository."

git -C "$REPO_DIR" fetch --quiet origin "$MAIN_BRANCH" ||
    fail "Could not fetch main branch."

git -C "$REPO_DIR" checkout --quiet -B "$MAIN_BRANCH" "origin/$MAIN_BRANCH" ||
    fail "Could not checkout main branch."

[[ -d "$REPO_DIR/$DOCS_PATH" ]] ||
    fail "Documentation directory does not exist: $DOCS_PATH"

CURRENT_SHA="$(git -C "$REPO_DIR" rev-parse HEAD)"

LAST_SHA=""

if [[ -f "$STATE_FILE" ]]; then
    LAST_SHA="$(cat "$STATE_FILE")"
fi

log "Current main SHA: $CURRENT_SHA"

# --------------------------------------------------
# Check whether anything changed
# --------------------------------------------------

if [[ "$CURRENT_SHA" == "$LAST_SHA" ]]; then
    log "No new commits. Nothing to do."
    exit 0
fi

CHANGE_BASE=""

if [[ -n "$LAST_SHA" ]] &&
    git -C "$REPO_DIR" cat-file -e "${LAST_SHA}^{commit}" 2>/dev/null &&
    git -C "$REPO_DIR" merge-base --is-ancestor "$LAST_SHA" "$CURRENT_SHA"; then


    CHANGE_BASE="$LAST_SHA"

    CHANGE_NOTE="Changes since the previous successful documentation update."

else

    CHANGE_BASE="$(git -C "$REPO_DIR" rev-list --max-parents=0 "$CURRENT_SHA" | tail -n 1)"

    CHANGE_NOTE="Initial run or previous checkpoint unavailable. Review the current PizzaProject architecture and recent history carefully."

fi

log "Change base: $CHANGE_BASE"

# --------------------------------------------------
# Prepare agent prompt
# --------------------------------------------------

if [[ "$PR_CREATE" == "true" ]]; then
    PR_STEP="If documentation changed and gh is authenticated: create a pull request to ${MAIN_BRANCH} with gh pr create. Never merge it."
else
    PR_STEP="Do not create a pull request in this run."
fi

PROMPT="$(cat <<EOF
Fresh clone of this repository, checked out on the latest ${MAIN_BRANCH}.
You are the architecture documentation agent for it.

First read AGENT.md and the skills under .agents/skills/ - they define the
rules, the evidence requirements and the exact workflow to follow.

Scope: documentation only, for the PizzaProject subproject.
Target directory: ${DOCS_PATH} (arc42).
Do not document the bot automation (local-agent/, *.sh, *.yml) or the agent
instructions (AGENT.md, .agents/) - they are tooling, not architecture.
Change range to review: ${CHANGE_BASE}..${CURRENT_SHA}
${CHANGE_NOTE}

Follow the workflow in AGENT.md step by step:
1. Review the changes: git diff and git log over the change range.
2. Assess whether the changes affect the PizzaProject architecture documentation.
3. Read the current arc42 chapters in ${DOCS_PATH}.
4. Update only stale or incomplete chapters. If no update is needed,
   change no file and stop after reporting why.
5. If documentation changed: BEFORE staging or committing, create and switch
   to a new docs/arc42-auto-* branch from origin/${MAIN_BRANCH}. Verify with
   git branch --show-current before proceeding. Never commit on ${MAIN_BRANCH}
   and then create a branch pointing to that commit. Commit only files under
   ${DOCS_PATH}; verify the branch again immediately before commit, then push
   explicitly using git push -u origin <docs-branch> (never push ${MAIN_BRANCH}).
6. ${PR_STEP}
7. Report: updated chapters and why, evidence paths, checks performed,
   open questions, and the PR URL if a pull request was created.

Hard rules:
- Never push to ${MAIN_BRANCH} and never force-push.
- Create/switch to the docs branch before staging or committing. If the current
  branch is ${MAIN_BRANCH} at commit time, stop without committing or pushing.
- If a commit was accidentally made on ${MAIN_BRANCH}, stop and report it; do
  not push or rewrite/repair ${MAIN_BRANCH} without maintainer approval.
- Never modify files outside ${DOCS_PATH}.
- Base every statement on repository evidence; never invent architecture.
- Do not claim a push or a pull request succeeded unless the command did.
EOF
)"

# --------------------------------------------------
# Run AI agent
# --------------------------------------------------

log "Starting AI agent..."

set +e

(
    cd "$REPO_DIR"
    "${AGENT_CMD_ARGS[@]}" "$PROMPT"
)

AGENT_STATUS=$?

set -e

if [[ "$AGENT_STATUS" -ne 0 ]]; then
    fail "AI agent failed with exit code $AGENT_STATUS. Checkpoint not updated."
fi

log "AI agent finished successfully."

# --------------------------------------------------
# Guard: the agent must not have moved the remote main branch
# --------------------------------------------------

git -C "$REPO_DIR" fetch --quiet origin "$MAIN_BRANCH" ||
    fail "Could not fetch ${MAIN_BRANCH} after the agent run."

ORIGIN_MAIN_SHA="$(git -C "$REPO_DIR" rev-parse "origin/${MAIN_BRANCH}")"

if [[ "$ORIGIN_MAIN_SHA" != "$CURRENT_SHA" ]]; then
    fail "Remote ${MAIN_BRANCH} moved during the agent run (${CURRENT_SHA} -> ${ORIGIN_MAIN_SHA}). Automatic processing stopped. Checkpoint not updated."
fi

log "Guard passed: remote ${MAIN_BRANCH} untouched."

# --------------------------------------------------
# Save successful checkpoint
# --------------------------------------------------

printf '%s\n' "$CURRENT_SHA" > "${STATE_FILE}.tmp"
mv "${STATE_FILE}.tmp" "$STATE_FILE"

log "Checkpoint updated: $CURRENT_SHA"

log "Documentation update run completed."
