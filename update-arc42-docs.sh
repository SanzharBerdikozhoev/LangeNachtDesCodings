
#!/usr/bin/env bash

# One-shot AI documentation updater.
# Triggered by GitHub Webhook through FastAPI.
#
# Requirements:
#   git, bash, flock, opencode
#   gh (optional, for automatic Pull Request creation)
#
# Authentication:
#   Configure GitHub CLI and git credentials beforehand.
#
# The script:
#   1. Fetches the latest main branch.
#   2. Compares it with the previous successful run.
#   3. Runs OpenCode to update arc42 documentation.
#   4. Checks that only documentation files were changed.
#   5. Creates a separate branch, commits and pushes changes.
#   6. Optionally creates a Pull Request.
#
# It processes exactly one update and then exits.

set -Eeuo pipefail

# --------------------------------------------------
# Configuration
# --------------------------------------------------

REPO_URL="${REPO_URL:-https://github.com/SanzharBerdikozhoev/LangeNachtDesCodings.git}"

MAIN_BRANCH="${MAIN_BRANCH:-main}"

DOCS_PATH="${DOCS_PATH:-PizzaProject_Tashbaev_Kurbanov_Berdikozhoev/docs/arc42}"

AGENT_CMD="${AGENT_CMD:-opencode run}"

PR_CREATE="${PR_CREATE:-true}"

WORKDIR_ROOT="${WORKDIR_ROOT:-${TMPDIR:-/tmp}/arc42-doc-agent}"

STATE_DIR="${STATE_DIR:-${XDG_STATE_HOME:-$HOME/.local/state}/arc42-doc-agent}"

STATE_FILE="${STATE_FILE:-${STATE_DIR}/last-processed-main}"

LOCK_FILE="${LOCK_FILE:-${STATE_DIR}/agent.lock}"

GIT_AUTHOR_NAME="${GIT_AUTHOR_NAME:-Arc42 Documentation Bot}"

GIT_AUTHOR_EMAIL="${GIT_AUTHOR_EMAIL:-arc42-doc-agent@users.noreply.github.com}"

# --------------------------------------------------
# Initialization
# --------------------------------------------------

mkdir -p "$WORKDIR_ROOT"
mkdir -p "$STATE_DIR"

log() {
    printf '[%s] %s\n' "$(date -Is)" "$*" >&2
}

fail() {
    log "ERROR: $*"
    exit 1
}

# Prevent simultaneous executions.
exec 9>"$LOCK_FILE"

if ! flock -n 9; then
    log "Another documentation update is already running."
    exit 0
fi

RUN_DIR=""

cleanup() {
    if [[ -n "${RUN_DIR:-}" && -d "${RUN_DIR:-}" ]]; then
        rm -rf -- "$RUN_DIR"
    fi
}

trap cleanup EXIT

# --------------------------------------------------
# Dependency checks
# --------------------------------------------------

for cmd in git bash flock mktemp; do
    command -v "$cmd" >/dev/null 2>&1 ||
        fail "Required command not found: $cmd"
done

# Check OpenCode command.
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

PROMPT="$(cat <<EOF
You are an architecture documentation agent working on this repository.

Your task is to maintain the arc42 architecture documentation.

IMPORTANT SCOPE:

- Work ONLY on the PizzaProject subproject.
- Do not document sibling projects.
- Target documentation directory: ${DOCS_PATH}
- Do not modify application code, build files, tests, or unrelated files.

INSTRUCTIONS:

1. Read these instruction files if they exist:
   - AGENT.md
   - GITHUB_SKILLS.md
   - ARCHITECTURE_SKILLS.md

2. Inspect the Git history and changes.

   Main branch before changes:
   ${CHANGE_BASE}

   Current main commit:
   ${CURRENT_SHA}

   Change note:
   ${CHANGE_NOTE}

   Use git log and git diff to inspect the relevant changes.
   Inspect current source files when necessary.

3. Determine whether the changes affect architecture documentation.

4. If documentation needs updating:
   - Update only affected arc42 chapters.
   - Preserve the existing 12-chapter structure.
   - Follow the existing language and formatting conventions.
   - Do not invent architecture, design decisions, integrations,
     deployment details, quality goals, or tests.
   - Base every statement on actual repository evidence.
   - Keep documentation consistent with the current implementation.

5. If no documentation update is necessary:
   - Do not modify any files.
   - Explain why no update is needed.

6. IMPORTANT GIT RULES:

   - Do NOT create commits.
   - Do NOT create branches.
   - Do NOT push anything.
   - Do NOT create Pull Requests.
   - Do NOT modify files outside ${DOCS_PATH}.

   The surrounding automation script handles Git operations.

7. Before finishing:
   - Inspect git status.
   - Review all modifications.
   - Check that only intended documentation files changed.
   - Do not include secrets or credentials.

8. Final response must report:
   - Whether documentation was updated.
   - Which arc42 chapters were changed.
   - Evidence paths used.
   - Checks performed.
   - Remaining uncertainties.
   - Why no update was necessary, if applicable.

Do not claim that you committed, pushed, or created a Pull Request.
EOF
)"

# --------------------------------------------------
# Run OpenCode
# --------------------------------------------------

log "Starting OpenCode..."

set +e

(
    cd "$REPO_DIR"
    "${AGENT_CMD_ARGS[@]}" "$PROMPT"
)

AGENT_STATUS=$?

set -e

if [[ "$AGENT_STATUS" -ne 0 ]]; then
    fail "OpenCode failed with exit code $AGENT_STATUS. Checkpoint not updated."
fi

log "OpenCode finished successfully."

# --------------------------------------------------
# Validate agent modifications
# --------------------------------------------------

ACTUAL_SHA="$(git -C "$REPO_DIR" rev-parse HEAD)"

if [[ "$ACTUAL_SHA" != "$CURRENT_SHA" ]]; then
    fail "Agent modified Git history. Automatic processing stopped."
fi

CHANGED_FILES=()

while IFS= read -r line; do

    [[ -z "$line" ]] && continue

    FILE="${line:3}"

    case "$FILE" in
        "$DOCS_PATH"|"$DOCS_PATH"/*)
            CHANGED_FILES+=("$FILE")
            ;;
        *)
            fail "Unexpected file modification outside documentation: $FILE"
            ;;
    esac

done < <(git -C "$REPO_DIR" status --porcelain --untracked-files=all)

# --------------------------------------------------
# No documentation changes
# --------------------------------------------------

if [[ ${#CHANGED_FILES[@]} -eq 0 ]]; then

    log "OpenCode found no documentation changes."


    printf '%s\n' "$CURRENT_SHA" > "${STATE_FILE}.tmp"
    mv "${STATE_FILE}.tmp" "$STATE_FILE"

    log "Checkpoint updated: $CURRENT_SHA"

    exit 0
fi

log "Documentation files changed:"

for file in "${CHANGED_FILES[@]}"; do
    log "  $file"
done

# --------------------------------------------------
# Create documentation branch
# --------------------------------------------------

BRANCH="docs/arc42-auto-$(date -u +%Y%m%dT%H%M%SZ)"

git -C "$REPO_DIR" switch --quiet -c "$BRANCH" ||
    fail "Could not create documentation branch."

# --------------------------------------------------
# Stage only arc42 documentation
# --------------------------------------------------

git -C "$REPO_DIR" add -- "$DOCS_PATH" ||
    fail "Could not stage documentation."

if git -C "$REPO_DIR" diff --cached --quiet; then

    log "No tracked documentation changes to commit."

    printf '%s\n' "$CURRENT_SHA" > "${STATE_FILE}.tmp"
    mv "${STATE_FILE}.tmp" "$STATE_FILE"

    exit 0
fi

git -C "$REPO_DIR" diff --cached --check ||
    fail "Whitespace or patch errors detected."

# Verify staged files.
while IFS= read -r file; do

    case "$file" in
        "$DOCS_PATH"|"$DOCS_PATH"/*)
            ;;
        *)
            fail "Unexpected staged file: $file"
            ;;
    esac

done < <(git -C "$REPO_DIR" diff --cached --name-only)

# --------------------------------------------------
# Commit
# --------------------------------------------------

git -C "$REPO_DIR" \
    -c user.name="$GIT_AUTHOR_NAME" \
    -c user.email="$GIT_AUTHOR_EMAIL" \
    commit -m "docs(arc42): update architecture documentation" ||
    fail "Could not create documentation commit."

log "Documentation committed."

# --------------------------------------------------
# Push branch
# --------------------------------------------------

log "Pushing branch: $BRANCH"

git -C "$REPO_DIR" push --set-upstream origin "$BRANCH" ||
    fail "Push failed. Checkpoint not updated."

log "Branch pushed successfully."

# --------------------------------------------------
# Create Pull Request
# --------------------------------------------------

if [[ "$PR_CREATE" == "true" ]]; then

    if command -v gh >/dev/null 2>&1 &&
       gh auth status >/dev/null 2>&1; then

        log "Creating Pull Request..."

        if PR_URL="$(
            gh pr create \
                --repo "$REPO_URL" \
                --base "$MAIN_BRANCH" \
                --head "$BRANCH" \
                --title "docs(arc42): update architecture documentation" \
                --body "Automated arc42 documentation update.

Main commit reviewed: ${CURRENT_SHA}

Updated directory: ${DOCS_PATH}

Please review the documentation changes before merging."
        )"; then

            log "Pull Request created: $PR_URL"

        else

            log "WARNING: Branch was pushed, but Pull Request creation failed."

        fi

    else

        log "WARNING: GitHub CLI unavailable or not authenticated."
        log "Branch was pushed, but no Pull Request was created."

    fi

fi

# --------------------------------------------------
# Save successful checkpoint
# --------------------------------------------------

printf '%s\n' "$CURRENT_SHA" > "${STATE_FILE}.tmp"
mv "${STATE_FILE}.tmp" "$STATE_FILE"

log "Checkpoint updated: $CURRENT_SHA"

log "Documentation update completed successfully."
