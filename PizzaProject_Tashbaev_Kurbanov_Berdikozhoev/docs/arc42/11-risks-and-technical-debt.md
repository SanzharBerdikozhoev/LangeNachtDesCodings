# 11. Risks and Technical Debt

The following are repository-evidenced concerns, not assertions that they have caused incidents:

| Risk / debt | Evidence and impact to verify |
|---|---|
| Payment credentials are configured as plain text | `src/main/resources/application.properties` defines `service.payment.plainCreds` with a credential-like value. The value is not reproduced. Confirm it is not an active credential and use deployment-appropriate secret injection. |
| H2 console enabled | `application.properties` enables `/h2-console`; `SecurityConfig` excludes H2 console paths from Spring Security. Confirm this is restricted appropriately in any non-development environment. |
| Sample identities/passwords seeded | `common/db/InitializeDB.java` seeds admin/user accounts with literal passwords (then encoded). Confirm these credentials and initialization behavior are development-only or otherwise secured. |
| Payment/database consistency | `OrderedServiceImpl` removes cart items before payment succeeds, calls the remote payment API, then saves the order. Database rollback cannot undo a successful remote payment; failure/retry/reconciliation behavior is not documented. Validate transactional behavior and recovery requirements. |
| Payment client resilience not evident | `PaymentServiceImpl` creates a default `RestClient`; no explicit timeout, retry, circuit breaker, or idempotency handling is visible in this class. |
| Database/deployment configuration unclear | H2 is a runtime dependency, but no explicit production datasource profile or deployment configuration was found in the inspected project files. |
| No documented architecture rationale or quality targets | No ADRs, explicit measurable quality requirements, or production environment configuration are present in the project. Confirm intended operational requirements and record significant decisions. |

These items need confirmation against intended environments and operational requirements.
