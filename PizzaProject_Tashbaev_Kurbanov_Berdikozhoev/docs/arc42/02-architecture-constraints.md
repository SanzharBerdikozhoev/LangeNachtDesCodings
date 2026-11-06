# 2. Architecture Constraints

Constraints directly evidenced by project configuration and code:

| Constraint | Evidence |
|---|---|
| Java 21 toolchain | `build.gradle` configures `JavaLanguageVersion.of(21)`. |
| Spring Boot application | `build.gradle` uses Spring Boot 3.5.6; application entry point is `src/main/java/whz/pti/eva/PizzaProjectTashbaevKurbanovBerdikozhoevApplication.java`. |
| Web UI uses Spring MVC and Thymeleaf | Dependencies in `build.gradle`; views and static resources are in `src/main/resources/templates` and `static`. |
| Persistence uses Spring Data JPA; H2 is a runtime dependency | `build.gradle`; JPA entities and repositories are in feature packages. |
| Authentication and authorization use Spring Security | Security dependency and `auth/SecurityConfig.java`. |
| Build and tests use Gradle/JUnit platform | Gradle wrapper/build script and `tasks.named('test')` in `build.gradle`. |

No external contractual, regulatory, hosting, or organizational constraints are established by the available repository documentation.
