# Arc42 Documentation Bot (monorepo scenario)

This repository contains both the automation bot and the documented project
(`PizzaProject_Tashbaev_Kurbanov_Berdikozhoev/`). The bot watches **this
repository's** `main` branch and keeps the arc42 architecture documentation of
the PizzaProject subproject in sync with its code.

> Alternative scenario: a separate bot repository watching a separate project
> repository — see the `fix/reduce-prompt-folders` branch.

## How it works

1. Someone pushes to `main` of this repository.
2. GitHub delivers a signed `push` webhook to the FastAPI server (`local-agent/main.py`).
3. The server verifies the signature and checks that the event is a push to `main`, then schedules a background task.
4. `update-arc42-docs.sh` clones this repository into a temporary directory and determines the change range since the previous successful run.
5. The AI agent (OpenCode by default) runs inside the clone. Following the workflow in `AGENT.md` it:
   1. reviews the changes (`git diff` / `git log`),
   2. assesses the impact on the PizzaProject architecture documentation,
   3. reads the current arc42 chapters,
   4. updates only the stale chapters,
   5. creates a `docs/arc42-auto-*` branch, commits, and pushes it,
   6. opens a pull request (never merges),
   7. reports the result.
6. The script verifies that the remote `main` was not touched by the agent and saves a checkpoint (last processed main SHA). The temporary clone is removed.
7. A human reviews and merges the pull request.

## Contents

- `local-agent/` — FastAPI webhook server (`main.py`)
- `update-arc42-docs.sh` — one-shot update runner (triggered by the webhook, or manually)
- `update-arc42.yml` — legacy polling runner (alternative mode, superseded by the webhook)
- `AGENT.md`, `.agents/skills/` — agent instructions and skills (read by the agent inside its clone)
- `PizzaProject_Tashbaev_Kurbanov_Berdikozhoev/` — the documented project, docs in `docs/arc42/`

## Setup

### 1. Install

```bash
python3 -m venv local-agent/venv
local-agent/venv/bin/pip install -r local-agent/requirements.txt
```

The runner also needs `git`, `bash`, the AI CLI (`opencode` by default,
configurable via `AGENT_CMD`), and for automatic PR creation `gh`
authenticated with a token that can read the repository, push branches, and
create pull requests. `flock` is optional — without it (e.g. on macOS) the
script falls back to a portable `mkdir`+pid lock. Do **not** grant merge or
force-push permissions.

### 2. Configure the webhook secret

```bash
cp local-agent/.env.example local-agent/.env   # or create local-agent/.env
# set GITHUB_WEBHOOK_SECRET to any random string
```

### 3. Start the server

```bash
cd local-agent
venv/bin/uvicorn main:app --host 0.0.0.0 --port 8000
```

### 4. Create the webhook

In this repository on GitHub: *Settings → Webhooks → Add webhook*.

- Payload URL: `https://<your-public-url>/github-webhook` (tunnel/port-forward as needed)
- Content type: `application/json`
- Secret: the same value as `GITHUB_WEBHOOK_SECRET`
- Events: *Only select events* → `push`

### Manual trigger

The pipeline can be run without the webhook:

```bash
bash update-arc42-docs.sh
```

## Design notes

- One webhook push triggers one script run; concurrent runs are blocked by a lock in the script and a task lock in the server.
- The checkpoint (last processed main SHA) is stored under `~/.local/state/arc42-doc-agent/`. On agent failure the checkpoint is not updated, so the same change range is retried on the next push.
- Documentation changes always land on a `docs/arc42-auto-*` branch and a pull request; `main` is never modified directly. After every run the script re-fetches `origin/main` and fails if it moved.
- No infinite loop: the webhook only reacts to pushes on `main`; the agent pushes documentation branches only. When a docs PR is merged, the next run reviews documentation-only changes and reports "no update needed".
- Human review and merge remain required.
- The temporary clone is deleted after each run.
