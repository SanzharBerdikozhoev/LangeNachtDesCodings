# 10. Quality Requirements

## 10.1 Explicit requirements

No measurable quality requirements, service-level objectives, or acceptance criteria are documented in the repository.

## 10.2 Observable quality-related implementation

| Quality aspect | Evidence / limitation |
|---|---|
| Testability | JUnit/Spring Boot test dependencies and feature-level service/controller tests exist under `src/test/java`. Test presence does not establish coverage or quality targets. |
| Access control | Spring Security URL rules and method security are enabled in `auth/SecurityConfig.java`; no threat model or security test coverage analysis is documented here. |
| Persistence consistency | Checkout service is annotated `@Transactional` for local persistence work. Payment is a remote HTTP side effect and has no distributed transaction guarantee shown. |
| Error handling | Domain exceptions are present; payment exceptions are mapped in code. No complete error-handling policy is specified. |
| Operability/performance/availability | No targets, monitoring, logging policy, capacity model, or resilience configuration are documented. |

Quality goals and their prioritization require stakeholder clarification.
