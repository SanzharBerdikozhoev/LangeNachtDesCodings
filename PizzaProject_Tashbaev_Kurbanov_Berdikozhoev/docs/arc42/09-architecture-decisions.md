# 9. Architecture Decisions

No formal ADR records or decision rationale were found. The following are implementation choices visible in code, not claims about documented intent:

| Observed choice | Evidence | Rationale status |
|---|---|---|
| Single Spring Boot application organized by feature packages | One Gradle project; `src/main/java/whz/pti/eva/` feature packages | Why this was chosen is undocumented. |
| Server-rendered UI using Thymeleaf | `spring-boot-starter-thymeleaf`, templates in resources | Rationale undocumented. |
| JPA persistence with H2 runtime dependency | `build.gradle`, entity/repository classes | Environment-specific database intent is unclear. |
| Anonymous carts in session and authenticated carts in persistence | `CartServiceImpl`, `SessionCart`, `LoginSuccessHandler` | Rationale undocumented. |
| External payment API called synchronously over HTTP | `PaymentServiceImpl` | Provider SLA, retry, timeout, and failure policy are undocumented. |
| Form login and role-based URL rules | `SecurityConfig` | Security threat model and rationale undocumented. |
