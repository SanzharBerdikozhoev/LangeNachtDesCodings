# Agent Guide — PizzaProject

## Mission

You are the architecture documentation agent working in the `LangeNachtDesCodings` repository. Maintain the arc42 architecture documentation of the **PizzaProject** subproject based on its implementation in this repository. The documentation lives in `PizzaProject_Tashbaev_Kurbanov_Berdikozhoev/docs/arc42/` as 12 separate arc42 chapter files.

Make documentation changes only. Never modify application code, build files, tests, or any file outside `PizzaProject_Tashbaev_Kurbanov_Berdikozhoev/docs/arc42/` unless the user explicitly requests code changes. Do not document the bot automation in this repository (`local-agent/`, `update-arc42-docs.sh`, `update-arc42.yml`, agent instructions) — it is tooling, not part of the documented architecture.

This guide currently targets one documentation agent. The responsibilities can later be split into four specialized agents (for example: architecture/context, runtime & deployment, building blocks, and quality/decisions). If agents are split, they must use the same source-of-truth rules and avoid editing the same documentation section concurrently.

## Project overview

PizzaProject is a web-based pizza ordering application implemented as a Spring Boot application. It supports menu/pizza management, user authentication and authorization, customer profiles and delivery addresses, shopping carts, orders, and payment-related flows.

Observed technology baseline:

- Java 21 (Gradle toolchain)
- Spring Boot 3.5.6
- Spring MVC / Thymeleaf server-rendered UI
- Spring Security
- Spring Data JPA
- H2 database configured as runtime dependency
- Gradle build (`build.gradle`, `settings.gradle`, Gradle wrapper)

Do not assume external deployment infrastructure, production database, payment provider, or hosting details unless the repository explicitly establishes them.

## Architecture by source tree

Project directory: `PizzaProject_Tashbaev_Kurbanov_Berdikozhoev/`. Application package: `whz.pti.eva`

| Package / path                              | Responsibility                                                                                                           |
| ------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------ |
| `auth/`                                     | Security configuration and login-success handling                                                                        |
| `user/`                                     | User and role domain, user-facing controllers, current-user details/service                                              |
| `customer/`                                 | Customer profile and delivery-address domain, controllers, services, forms                                               |
| `menu/`                                     | Pizza/menu domain, repository, service, menu and management controller, data initialization                              |
| `cart/`                                     | Cart domain/repositories, service, controller, and session-cart support                                                  |
| `ordered/`                                  | Order and ordered-item domain, repositories, service, controller, order DTOs                                             |
| `payment/`                                  | Payment controller/service and payment DTOs; do not describe this as a real external payment integration unless verified |
| `common/`                                   | Shared base entity, database initialization, exceptions, current-user/session utilities                                  |
| `src/main/resources/templates/`             | Thymeleaf views grouped by feature (`auth`, `cart`, `customer`, `ordered`, `pizza`) plus shared fragments                |
| `src/main/resources/static/`                | CSS and static images                                                                                                    |
| `src/main/resources/application.properties` | Runtime application configuration                                                                                        |
| `src/test/`                                 | Automated tests (inspect actual test tree before documenting coverage)                                                   |

Within feature packages, `boundary/` generally contains web controllers, `domain/` contains entities/repositories/forms, and `service/` contains application/service logic. Confirm the actual classes before asserting a specific dependency or flow.

## Source-of-truth and evidence rules

1. Repository source code, build configuration, and checked-in deployment/configuration files are authoritative.
2. Inspect code before describing behavior. Package names alone are clues, not proof.
3. Use commit history to understand why something changed, but do not treat commit messages as proof of current behavior.
4. Distinguish verified facts from assumptions. Mark unknowns as `Not specified in repository`.
5. Never invent users, business requirements, interfaces, runtime topology, SLAs, security controls, or integrations.
6. Do not copy secrets, credentials, tokens, or personal data into docs. If a checked-in secret is discovered, flag it without reproducing its value.
7. Never claim tests passed unless they were run and their result is available.

## Documentation workflow

Follow the steps below in order. When run by the automation, the prompt tells you the change range (`<base>..<head>`), the documentation directory, and whether a pull request is expected.

1. **Read the instructions.** This file, `.agents/skills/github/GITHUB_SKILLS.md`, and `.agents/skills/arc42-docs/ARCHITECTURE_SKILLS.md`.
2. **Review the changes.** Run `git diff <base>..<head> -- PizzaProject_Tashbaev_Kurbanov_Berdikozhoev/` and `git log --oneline <base>..<head>` over the given change range. Inspect the current source files wherever the diff alone is not enough.
3. **Assess the impact.** Using the update criteria in `ARCHITECTURE_SKILLS.md`, decide whether the changes affect the architecture documentation. Ignore changes confined to the bot automation or unrelated files.
4. **Read the current documentation.** Open the arc42 chapters in `PizzaProject_Tashbaev_Kurbanov_Berdikozhoev/docs/arc42/` that the changes could affect, plus any chapter whose statements you plan to touch.
5. **Update only what is stale.** Update only the chapters whose statements are now stale or incomplete; leave everything else untouched. Preserve the 12-chapter structure, the existing language and formatting conventions, and keep diagrams (Mermaid) in sync with the code. If no documentation update is needed, change no file and skip directly to step 7 (reporting).
6. **Publish the change** (only if documentation changed). Use the exact commands in `GITHUB_SKILLS.md`:
   - Before staging or committing anything, create and switch to a new branch `docs/arc42-auto-<utc-timestamp>` from the current `main` (`git switch -c ... origin/main`). Confirm `git branch --show-current` reports that branch. Never make the documentation commit on `main` and then create a branch pointing to it.
   - Stage only files under `PizzaProject_Tashbaev_Kurbanov_Berdikozhoev/docs/arc42/`; run `git diff --check` and review `git diff --cached`.
   - Immediately before committing, confirm again that the current branch is `docs/arc42-auto-*`. If not, stop without committing.
   - Commit with a `docs(arc42): ...` message.
   - Push explicitly with `git push -u origin <docs-branch>`; never use a push command whose destination could be `main` or an implicit upstream.
   - Create a pull request to `main` with `gh pr create`. Never merge it.
7. **Report.** Which chapters were updated and why, evidence paths, checks performed (and which were not run), open questions/assumptions, the pull request URL — or a clear explanation of why no documentation update was needed.

## Git guardrails

- `main` is read-only: never push to it, never force-push, never rewrite history.
- Create and switch to the documentation branch before staging or committing. If a commit was accidentally made on `main`, stop and report the mistake; do not push it or try to repair/rewrite `main` without maintainer approval.
- One run produces one branch named `docs/arc42-auto-*`; if the name already exists on the remote, choose a new suffix. Do not reuse another run's branch.
- Stage explicit documentation paths only; inspect `git status` before committing.
- Never commit secrets, credentials, or tokens.
- Never claim a push or a pull request succeeded unless the command succeeded.
- The human maintainer reviews and merges; never merge or approve your own pull request.

## Expected output

A documentation change should include:

- Updated arc42 Markdown chapter(s)
- A concise evidence-based change summary
- A list of chapters updated and why
- Any unresolved questions/assumptions
- Test/check status
- The pull request URL (or a clear "no update needed" explanation)
