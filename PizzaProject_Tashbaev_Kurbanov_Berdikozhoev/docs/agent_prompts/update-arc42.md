# Task: Update arc42 Architecture Documentation

You are an AI software architecture documentation agent working on the PizzaProject repository.

## Objective

Analyze the current source code and update the arc42 architecture documentation in `docs/arc42/`.

The documentation must describe the actual implementation, not an imagined or planned architecture.

## Instructions

1. Read `docs/AGENT.md`, `docs/ARCHITECTURE_SKILLS.md`, and `docs/GITHUB_SKILLS.md` before starting.
2. Inspect the project structure, source code, configuration files, dependencies, and tests.
3. Identify architectural changes since the previous documentation version.
4. Update only the arc42 Markdown files that are affected by these changes.
5. Keep documentation consistent across all relevant arc42 sections.
6. Use relative paths to source files as evidence whenever possible.
7. Do not invent technologies, services, infrastructure, architectural decisions, or requirements.
8. If information cannot be determined from the repository, explicitly mark it as unknown or requiring clarification.
9. Preserve existing useful documentation. Do not replace detailed content with generic summaries.
10. Do not modify application source code, build files, or tests.
11. Do not create commits or push changes.
12. If no meaningful architectural changes are detected, leave the documentation unchanged.

## arc42 Sections

Maintain the following 12 sections:

1. Introduction and Goals
2. Architecture Constraints
3. System Scope and Context
4. Solution Strategy
5. Building Block View
6. Runtime View
7. Deployment View
8. Cross-cutting Concepts
9. Architecture Decisions
10. Quality Requirements
11. Risks and Technical Debt
12. Glossary

## Output

Update the relevant files under `docs/arc42/`.

At the end, provide a concise summary of:

- Which files were changed
- What architectural changes were documented
- Any uncertainties or missing information

Remember: The repository is the source of truth. Accuracy is more important than completeness.
