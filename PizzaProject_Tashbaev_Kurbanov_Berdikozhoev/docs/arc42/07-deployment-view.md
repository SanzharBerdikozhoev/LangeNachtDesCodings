# 7. Deployment View

## 7.1 Known from repository

- The application is packaged/built as a Spring Boot Gradle project (`build.gradle`, Gradle wrapper).
- H2 is declared as a runtime dependency and the H2 console is enabled at `/h2-console` in `application.properties`.
- The payment API base URL is configured as `http://localhost:9090/users/`.
- Static web assets and templates are packaged under `src/main/resources`.

## 7.2 Unknown / requiring clarification

The repository does not define or verify production hosting, operating system, container or VM usage, network topology, number of instances, reverse proxy, TLS termination, deployment pipeline, persistent database choice, backup/restore, or production secrets management. The configured localhost payment URL is not evidence of the actual deployed payment service location. No deployment diagram can be completed reliably from available files.
