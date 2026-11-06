# Agent Guide — PizzaProject

## Mission

Maintain the architecture documentation of **PizzaProject** based on the implementation in this repository. Documentation lives in `docs/` and is organized into the 12 arc42 sections. Make only documentation changes unless the user explicitly requests code changes.

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

Application package: `whz.pti.eva`

| Package / path | Responsibility |
|---|---|
| `auth/` | Security configuration and login-success handling |
| `user/` | User and role domain, user-facing controllers, current-user details/service |
| `customer/` | Customer profile and delivery-address domain, controllers, services, forms |
| `menu/` | Pizza/menu domain, repository, service, menu and management controller, data initialization |
| `cart/` | Cart domain/repositories, service, controller, and session-cart support |
| `ordered/` | Order and ordered-item domain, repositories, service, controller, order DTOs |
| `payment/` | Payment controller/service and payment DTOs; do not describe this as a real external payment integration unless verified |
| `common/` | Shared base entity, database initialization, exceptions, current-user/session utilities |
| `src/main/resources/templates/` | Thymeleaf views grouped by feature (`auth`, `cart`, `customer`, `ordered`, `pizza`) plus shared fragments |
| `src/main/resources/static/` | CSS and static images |
| `src/main/resources/application.properties` | Runtime application configuration |
| `src/test/` | Automated tests (inspect actual test tree before documenting coverage) |

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

1. Read `AGENT.md`, `GITHUB_SKILLS.md`, and `ARCHITECTURE_SKILLS.md`.
2. Read the relevant source files and the existing documentation before editing.
3. Review the target diff and recent commits. Identify whether changes affect architecture documentation.
4. Update only the arc42 chapters whose statements are now stale or incomplete. Keep unaffected sections untouched.
5. Keep diagrams source-editable (Mermaid or PlantUML text) and ensure labels match the code.
6. Run documentation checks (links/Markdown, if configured) and relevant build/tests only when feasible. Report any not run.
7. Review `git diff` for correctness, scope, secrets, and accidental code changes.
8. Create a dedicated branch and open a pull request. Never push documentation updates directly to `main`.

## Expected output

A documentation change should include:
- Updated arc42 Markdown chapter(s)
- A concise evidence-based change summary
- A list of chapters updated and why
- Any unresolved questions/assumptions
- Test/check status
