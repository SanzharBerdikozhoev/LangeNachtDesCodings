# PizzaProject — AI Arc42 Documentation Agent Kit

This kit was prepared for the `PizzaProject_Tashbaev_Kurbanov_Berdikozhoev` Spring Boot subproject in the uploaded archive. It deliberately excludes `GradesCalculator` and `MVP`.

## Contents

- `AGENT.md` — project overview, structure, source-of-truth rules, agent workflow
- `GITHUB_SKILLS.md` — repository reading, history, branches, commits, PR rules
- `ARCHITECTURE_SKILLS.md` — the arc42 12-chapter standard and update criteria
- `update-arc42-docs.sh` — polling runner that creates a disposable clone, invokes an AI CLI, and cleans up
- `README.md` — setup notes

## Recommended repository placement

Put the three instruction Markdown files in the PizzaProject repository root. Put generated chapter files in `docs/arc42/`. Place the shell script in a separate automation repository or on a trusted runner; avoid adding a polling daemon to the application runtime itself.

Expected docs tree:

```text
PizzaProject/
├── AGENT.md
├── GITHUB_SKILLS.md
├── ARCHITECTURE_SKILLS.md
├── docs/
│   └── arc42/
│       ├── 01-introduction-and-goals.md
│       ├── 02-architecture-constraints.md
│       ├── 03-system-scope-and-context.md
│       ├── 04-solution-strategy.md
│       ├── 05-building-block-view.md
│       ├── 06-runtime-view.md
│       ├── 07-deployment-view.md
│       ├── 08-concepts.md
│       ├── 09-architecture-decisions.md
│       ├── 10-quality-requirements.md
│       ├── 11-risks-and-technical-debt.md
│       └── 12-glossary.md
└── ...
```

## Runner setup

The runner requires `git`, the chosen AI CLI, and optionally `gh`. The clone URL and command are environment-specific:

```bash
export REPO_URL="https://github.com/OWNER/REPOSITORY.git"
export WORKDIR_ROOT="$HOME/.cache/arc42-doc-agent"
export AGENT_CMD="claude -p"
export POLL_SECONDS=300
chmod +x update-arc42-docs.sh
./update-arc42-docs.sh
```

The configured agent command is called with the complete prompt as its final argument. Different CLIs have different prompt flags; adapt `run_agent()` for your tool. For example, Claude Code, OpenCode, and pi do not all share a universal invocation format.

Use a GitHub token with minimum permissions needed to read the repository, push a branch, and create a pull request. Keep the token outside the repository (environment/secret manager). Do not provide merge or force-push permissions.

## Important design notes

- The script is a polling process, not a GitHub webhook. It checks `main` every `POLL_SECONDS`.
- It records a checkpoint after a successful agent invocation, so failures retry the same change range.
- Documentation changes are pushed to a new branch and PR; `main` is never changed directly.
- Human review and merge remain required.
- The temporary clone is deleted after each run.
- This is a starter implementation. For production, prefer a GitHub Actions workflow triggered by `push` to `main` or a scheduled runner with isolated credentials, logs, timeout, and concurrency controls.
