# 3. System Scope and Context

## 3.1 Business context

The implemented system supports pizza shopping and ordering. Browser-facing controllers render server-side views. The system also calls a separately addressed payment service; the provider and its operational ownership are not identified in this repository.

```text
Browser/user -- HTTP requests and rendered pages --> Pizza application
Pizza application -- HTTP payment/account requests --> Payment API
Pizza application -- JPA --> configured relational datasource (H2 dependency present)
```

The diagram describes code-level interactions, not a deployment topology. `payment/service/PaymentServiceImpl.java` constructs payment API URLs from `service.payment.url`. `application.properties` sets that URL to `http://localhost:9090/users/`.

## 3.2 Technical context

- UI: Thymeleaf templates under `src/main/resources/templates`, with static CSS/images under `src/main/resources/static`.
- Inbound web interface: Spring MVC controllers in feature `boundary` packages.
- Persistence interface: Spring Data repositories, JPA entities, and a configured datasource. H2 is a runtime dependency and H2 console is enabled at `/h2-console`; the effective datasource configuration beyond the repository properties should be confirmed at runtime.
- Outbound interface: HTTP via Spring `RestClient` to the payment API.

No API contract for external consumers, payment-service implementation, or production datasource configuration is included in the project files inspected.
