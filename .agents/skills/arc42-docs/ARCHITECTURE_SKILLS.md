---
name: arc42-documentation
description: >
  Analyze a software repository and create or update its architecture
  documentation using the 12-chapter arc42 structure. Use this skill
  when generating, reviewing, or synchronizing docs/arc42 files.
---

# Architecture Documentation Skills — arc42

## Purpose and structure

Use the arc42 template as a framework for describing the software architecture. Maintain the 12 chapters below as separate Markdown files in `docs/arc42/`:

1. `01-introduction-and-goals.md`
2. `02-architecture-constraints.md`
3. `03-system-scope-and-context.md`
4. `04-solution-strategy.md`
5. `05-building-block-view.md`
6. `06-runtime-view.md`
7. `07-deployment-view.md`
8. `08-concepts.md`
9. `09-architecture-decisions.md`
10. `10-quality-requirements.md`
11. `11-risks-and-technical-debt.md`
12. `12-glossary.md`

Do not create a chapter merely to fill space. If information is absent, explicitly state that it is not specified in the repository and identify what evidence would resolve it.

## Global writing rules

- Describe the current implemented architecture, not a desired future architecture, unless a section explicitly labels a target state.
- Every meaningful statement must be supported by code/configuration, an issue/decision record, or a verified project requirement.
- Cite repository evidence using relative file paths and, where useful, class/method names. Commit hashes may be included for historical rationale.
- Keep language concise, technical, and understandable to a new developer.
- Separate external actors, internal components, and external systems.
- Avoid describing an in-process class call as a network integration.
- Never infer production deployment, security assurance, availability, performance, or compliance from framework choice.
- Avoid duplicating long explanations across chapters; cross-link the canonical chapter.
- Update a diagram when its depicted structures or flows change.
- Use Mermaid for source-controlled diagrams unless the repository already standardizes another format.
- Do not expose secrets or sensitive data in documentation.
- Include “Verified”, “Assumption”, or “Open question” labels when certainty matters.

## Chapter-by-chapter authoring requirements

### 1. Introduction and Goals

Document the system's purpose, primary business capabilities, stakeholders, and the most important goals. Add a short scope statement. Use requirements only when supported by repository or project material. Do not invent a product vision.

### 2. Architecture Constraints

Record technical, organizational, and convention-based constraints that demonstrably shape the solution: Java/Gradle/Spring versions, required frameworks, mandated interfaces, or known course/project constraints. Distinguish hard constraints from choices. Cite `build.gradle` or other evidence.

### 3. System Scope and Context

Describe the system boundary, users/actors, and neighboring systems. Add a context diagram showing only verified interactions. If external payment, email, identity, or hosting systems are not established by the repository, mark them as unknown rather than inventing them.

### 4. Solution Strategy

Summarize key design approaches and why they are used: server-rendered web UI, feature-oriented packages, service/repository layering, persistence, session behavior, authentication, etc. Explain rationale only when it is documented or clearly supported; otherwise state that rationale is unknown.

### 5. Building Block View

Describe static decomposition at useful levels:

- Level 1: overall application and major feature areas
- Level 2: relevant package/component internals
- Include responsibilities and dependencies, not just a file list.
- Maintain diagrams and tables against actual source packages/classes.

### 6. Runtime View

Document representative runtime scenarios with sequence diagrams or step-by-step flows, such as:

- Login and session establishment
- Anonymous cart and authenticated cart behavior (only after verifying implementation)
- Menu browsing and order placement
- Payment-related flow
  Show participants and transitions actually supported by the code. Explain error paths when material.

### 7. Deployment View

Describe runtime nodes, deployment artifacts, configuration, infrastructure, and environments. Use repository evidence such as Docker/CI/cloud manifests. If none exist, document the known local execution setup and list production deployment as unspecified. Do not treat H2 configuration as proof of production deployment.

### 8. Concepts

Capture cross-cutting concepts: security/authentication, persistence, transaction boundaries, session management, validation/error handling, UI rendering, logging, and test strategy. Include only concepts found in the implementation. Link to relevant building blocks and runtime scenarios.

### 9. Architecture Decisions

Record significant decisions in an ADR-like table: decision, status, context, consequences, evidence, and date if known. Do not invent historical rationale. Where rationale cannot be recovered, write “Rationale not found; inferred from current implementation” and keep that inference clearly labeled. Avoid recording every minor implementation detail.

### 10. Quality Requirements

Document quality goals and scenarios (e.g. security, usability, maintainability, performance, testability) only if project requirements exist. For each measurable scenario include stimulus, environment, response, and measurable response criterion. If there are no explicit quality targets, say so and propose open questions rather than fabricating numbers.

### 11. Risks and Technical Debt

Identify evidence-backed risks, gaps, or uncertainties. Examples to investigate include local/default credentials, H2 console exposure, payment implementation boundaries, missing automated tests, or unverified production configuration. Never copy secret values. Distinguish confirmed findings from review candidates and describe impact/mitigation proportionately.

### 12. Glossary

Purpose. One shared vocabulary for all stakeholders, so the same word never means different things and different words never hide the same concept.

Questions answered

- What do the important domain terms and abbreviations mean?
- Which synonyms or naming conflicts exist (business language vs. UI vs. code), and which term is preferred?

What to write

- Define domain terms and abbreviations consistently (e.g. Pizza, Cart, Customer, Delivery Address, Ordered/Order, Role).
- Derive terms from code (class, table, API names) and user-facing language (UI labels, requirements).
- Mention synonyms and naming inconsistencies where they may confuse readers (e.g. "Cart" in the UI vs. Basket in the code) and name the preferred term.
- One short definition per term; link to the detailed section instead of repeating it.

| Term | Definition                                         | Synonyms / code name   | Used in     |
| ---- | -------------------------------------------------- | ---------------------- | ----------- |
| Cart | Pizzas selected by a customer but not yet ordered. | Basket (BasketService) | Sect. 5, UI |

Rules. Keep it alphabetical and limited to terms readers really need. Use the terms identically in all other sections. No architecture or decisions here (see sections 5, 8, 9).

## Documentation update decision

A commit should trigger a documentation update only when it changes or adds architecture-relevant information, such as:

- Package/module responsibility or dependency
- Domain model, persistence mapping, or schema
- Controller route, API/UI interaction, or runtime flow
- Authentication/authorization or session/cart behavior
- External integration, configuration, deployment, or build/runtime constraint
- Architectural decision, quality attribute, risk, or domain terminology

Usually no arc42 update is needed for:

- Formatting-only changes
- Purely internal refactoring that preserves documented structure and behavior
- Typo fixes in code with no architectural meaning
- Changes confined to unrelated sibling projects

The agent must still check whether existing docs become inaccurate. A “no documentation change” result should be reported briefly with reasons.
