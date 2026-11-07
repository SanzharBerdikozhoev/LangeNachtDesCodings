# Git Repository Skills

This document defines safe, repeatable GitHub operations for the documentation agent.

## Guardrails

- Treat `main` as protected and read-only for agent changes.
- Never use `git push --force`, rewrite shared history, delete remote branches, or commit secrets.
- Work only in a dedicated branch, with one task per branch.
- Do not stage the entire repository blindly. Stage explicit documentation paths or inspect `git status` first.
- If the repository is dirty before the task, preserve those changes; do not reset or clean them.
- Stop and report if the remote, permissions, branch base, or repository identity is unclear.
- Do not merge or approve your own pull request unless explicitly authorized.

## 1. Read repository files

Useful commands:

```bash
git status --short --branch
git ls-files
git ls-files 'PizzaProject_Tashbaev_Kurbanov_Berdikozhoev/**'
find . -maxdepth 3 -type f -not -path './.git/*' | sort
```

Read specific files without changing them:

```bash
sed -n '1,240p' path/to/file
cat path/to/file
```

Inspect `build.gradle`, `settings.gradle`, `src/main/java/`, `src/main/resources/`, and `src/test/` inside `PizzaProject_Tashbaev_Kurbanov_Berdikozhoev/`. Do not document the bot automation files (`local-agent/`, `update-arc42-docs.sh`, `update-arc42.yml`) or the agent instructions (`AGENT.md`, `.agents/`).

## 2. Inspect change history

```bash
git log --oneline --decorate -n 20
git log --oneline -- path/to/relevant/file
git show --stat --oneline <commit>
git show --format=fuller --find-renames <commit>
git diff --stat <base>..<head>
git diff --find-renames <base>..<head> -- PizzaProject_Tashbaev_Kurbanov_Berdikozhoev/
git diff --name-status <base>..<head>
```

For automation, use the explicit last-processed commit as `<base>` and the current `main` commit as `<head>`. If the base is missing or is not an ancestor of `main`, fall back to a bounded review of recent commits and record that limitation. Do not silently claim a complete diff.

## 3. Create a branch

Update local `main` and branch from it:

```bash
git fetch origin
git switch main
git pull --ff-only origin main
git switch -c docs/arc42-auto-YYYYMMDDTHHMMSSZ
```

In automated runs use the `docs/arc42-auto-<utc-timestamp>` pattern; for manual runs a descriptive, date-stamped name is fine. Use a branch name that is safe for a single run. If the branch already exists, choose a new suffix; do not reuse another run's branch.

## 4. Stage, commit, and push

```bash
git status --short
git diff --check
git diff -- docs/
git add docs/<specific-chapter>.md
git diff --cached --check
git diff --cached
git commit -m "docs(arc42): update <chapter>"
git push -u origin HEAD
```

Only commit documentation files under `PizzaProject_Tashbaev_Kurbanov_Berdikozhoev/docs/arc42/`. Never commit runtime state, temporary clone files, credentials, generated build output, or unrelated changes.

## 5. Create a pull request

GitHub CLI (`gh`) is the preferred method when installed and authenticated:

```bash
gh auth status
gh pr create \
  --base main \
  --head "$(git branch --show-current)" \
  --title "docs(arc42): update PizzaProject architecture documentation" \
  --body-file /tmp/pr-body.md
```

The PR body should summarize:
- Relevant source commits and files inspected
- Arc42 chapters updated
- Architectural facts changed (with paths as evidence)
- Checks performed and checks not performed
- Open questions / uncertainty

If `gh` is unavailable, provide the pushed branch and a compare/PR URL only if the repository's actual GitHub URL is known. Do not fabricate a URL.

## 6. Review PR state

```bash
gh pr view --json number,title,state,url,reviewDecision,statusCheckRollup
gh pr checks
```

Do not merge automatically. The human maintainer reviews and merges.
