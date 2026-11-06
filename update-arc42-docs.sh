#!/usr/bin/env bash
# Poll a GitHub repository's main branch, run an AI documentation agent in a
# disposable clone, and push a dedicated documentation branch + open a PR.
#
# Requirements: git, a configured AI CLI, and (for automatic PR creation) gh.
# Configure REPO_URL, WORKDIR_ROOT, AGENT_CMD, and optionally DOCS_PATH.
# The agent CLI must accept the prompt as its final argument. Example:
#   AGENT_CMD="-opencode run" ./update-arc42-docs.sh
# For another CLI, adapt AGENT_CMD to its prompt-input convention.
#
# Important: run this with a GitHub token that can push branches and create PRs.
# Do not give it permission to merge or force-push.

set -Eeuo pipefail

export REPO_URL="https://github.com/SanzharBerdikozhoev/LangeNachtDesCodings.git"
export MAIN_BRANCH="main"
export DOCS_PATH="PizzaProject_Tashbaev_Kurbanov_Berdikozhoev/docs/arc42"
export POLL_SECONDS="300"
export AGENT_CMD="opencode run"
export PR_CREATE="true"

: "${REPO_URL:?Set REPO_URL to the GitHub clone URL, e.g. https://github.com/org/repo.git}"
: "${WORKDIR_ROOT:=${TMPDIR:-/tmp}/arc42-doc-agent}"
: "${MAIN_BRANCH:=main}"
: "${DOCS_PATH:=docs/arc42}"
: "${POLL_SECONDS:=300}"
: "${AGENT_CMD="${AGENT_CMD:-opencode run}"
: "${STATE_FILE:=${WORKDIR_ROOT}/last-processed-main}"
: "${PR_CREATE:=true}"

mkdir -p "$WORKDIR_ROOT"
mkdir -p "$(dirname "$STATE_FILE")"

log() { printf '[%s] %s\n' "$(date -Is)" "$*" >&2; }

cleanup() {
  if [[ -n "${RUN_DIR:-}" && -d "${RUN_DIR:-}" ]]; then
    rm -rf -- "$RUN_DIR"
  fi
}
trap cleanup EXIT INT TERM

run_agent() {
  local repo_dir="$1"
  local prompt="$2"

  # AGENT_CMD is intentionally a configurable command string (e.g. "-opencode run"
  # or "opencode run"). Treat this environment variable as trusted configuration.
  # shellcheck disable=SC2086
  (cd "$repo_dir" && $AGENT_CMD "$prompt")
}

while true; do
  RUN_DIR="$(mktemp -d "${WORKDIR_ROOT}/run.XXXXXX")"
  REPO_DIR="${RUN_DIR}/repo"

  log "Fetching ${MAIN_BRANCH} from ${REPO_URL}"
  git clone --quiet --no-checkout "$REPO_URL" "$REPO_DIR"
  git -C "$REPO_DIR" fetch --quiet origin "$MAIN_BRANCH"
  git -C "$REPO_DIR" checkout --quiet -B "$MAIN_BRANCH" "origin/$MAIN_BRANCH"

  CURRENT_SHA="$(git -C "$REPO_DIR" rev-parse HEAD)"
  LAST_SHA=""
  [[ -f "$STATE_FILE" ]] && LAST_SHA="$(cat "$STATE_FILE")"

  if [[ "$CURRENT_SHA" == "$LAST_SHA" ]]; then
    log "No new commits on ${MAIN_BRANCH}."
    cleanup
    RUN_DIR=""
    sleep "$POLL_SECONDS"
    continue
  fi

  if [[ -n "$LAST_SHA" ]] && git -C "$REPO_DIR" cat-file -e "${LAST_SHA}^{commit}" 2>/dev/null \
      && git -C "$REPO_DIR" merge-base --is-ancestor "$LAST_SHA" "$CURRENT_SHA"; then
    CHANGE_BASE="$LAST_SHA"
    CHANGE_NOTE="Changes since last successful run (${LAST_SHA}..${CURRENT_SHA})."
  else
    # Initial run or non-linear history: use a bounded review, and explicitly
    # tell the agent the diff may not represent all changes since prior run.
    CHANGE_BASE="$(git -C "$REPO_DIR" rev-parse "${CURRENT_SHA}~10" 2>/dev/null || git -C "$REPO_DIR" rev-list --max-parents=0 HEAD | tail -n1)"
    CHANGE_NOTE="Initial run or previous checkpoint is not an ancestor. Review the available recent history; coverage may be incomplete."
  fi

  DIFF_FILE="${RUN_DIR}/main.diff"
  LOG_FILE="${RUN_DIR}/commits.log"
  git -C "$REPO_DIR" diff --find-renames "$CHANGE_BASE" "$CURRENT_SHA" > "$DIFF_FILE" || true
  git -C "$REPO_DIR" log --oneline --decorate "${CHANGE_BASE}..${CURRENT_SHA}" > "$LOG_FILE" || true

  if [[ ! -s "$DIFF_FILE" && ! -s "$LOG_FILE" ]]; then
    log "No diff/history to review; checkpointing current main."
    printf '%s\n' "$CURRENT_SHA" > "$STATE_FILE"
    cleanup
    RUN_DIR=""
    sleep "$POLL_SECONDS"
    continue
  fi

  BRANCH="docs/arc42-auto-$(date -u +%Y%m%dT%H%M%SZ)"
  git -C "$REPO_DIR" switch --quiet -c "$BRANCH"

  PROMPT="$(cat <<EOF
You are the architecture documentation agent for this repository.

Read AGENT.md, GITHUB_SKILLS.md, and ARCHITECTURE_SKILLS.md if present.
Scope: the PizzaProject subproject only. Do not document sibling projects.
Target arc42 docs directory: ${DOCS_PATH}

${CHANGE_NOTE}

Review the commit list and full diff supplied below. Then inspect current source files as needed:
--- COMMITS ---
$(cat "$LOG_FILE")
--- DIFF ---
$(cat "$DIFF_FILE")
--- END INPUT ---

Task:
1. Decide whether these changes affect architecture documentation under arc42.
2. If not, do not change documentation and explain why.
3. If yes, update only the affected arc42 chapters, preserving the 12-chapter structure.
4. Base every claim on repository evidence. Never invent architecture, deployment, integrations, rationale, quality targets, or tests.
5. Do not modify application code, build files, or unrelated projects.
6. Review git status and diff; ensure only intended documentation files changed and no secrets are included.
7. If documentation changed, run git diff --check, commit with a docs(arc42): message, and push this branch to origin.
8. If gh is installed and authenticated, create a pull request to ${MAIN_BRANCH}. Do not merge it.
9. Report updated chapters, evidence paths, checks, uncertainties, and PR URL (if created).
10. If no docs update is needed, leave the branch unpushed and report the reason.

Do not claim successful push or PR creation unless the command succeeded.
EOF
)"

  log "Running documentation agent on ${CURRENT_SHA}"
  set +e
  run_agent "$REPO_DIR" "$PROMPT"
  AGENT_STATUS=$?
  set -e

  if [[ "$AGENT_STATUS" -eq 0 ]]; then
    # Checkpoint only after the agent completed. This prevents skipping commits
    # if the CLI failed. Note: a PR may still require human review/merge.
    printf '%s\n' "$CURRENT_SHA" > "$STATE_FILE"
    log "Agent finished; checkpoint set to ${CURRENT_SHA}."
  else
    log "Agent exited with status ${AGENT_STATUS}; checkpoint retained for retry."
  fi

  cleanup
  RUN_DIR=""
  sleep "$POLL_SECONDS"
done
