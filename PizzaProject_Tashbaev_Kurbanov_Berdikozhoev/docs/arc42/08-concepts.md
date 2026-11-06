# 8. Concepts

## Authentication and authorization

`auth/SecurityConfig.java` configures form login, remember-me, logout, URL authorization, a DAO authentication provider, and a delegating password encoder. Static assets and H2 console paths are ignored by web security. URL rules distinguish anonymous/regular-user cart access, user-only order access, and admin-only pizza/customer management.

## Persistence and identity

JPA is used for domain persistence. Feature repositories extend Spring Data patterns. `common/domain/BaseEntity` is shared entity infrastructure. Login names are used as the user identity in cart/order service calls; the user entity has a unique non-null login name.

## Session state

The guest cart is represented by `SessionCart` / `SessionCartItem`; authenticated carts are database-backed. `CurrentUserUtil` and user services access current authentication context.

## Validation and error handling

Spring Boot validation is a dependency; form objects and a user creation validator exist. Domain-specific exceptions are in `common/exceptions`; payment balance errors also have controller advice. The exact handling varies by controller and is not unified in one documented global error model.

## External payment communication

`PaymentServiceImpl` uses Spring `RestClient`, JSON request bodies, and HTTP Basic credentials built from the `service.payment.plainCreds` property. The checked-in properties file contains a credential-like value; its value is intentionally not repeated here. Payment/deposit response errors are translated to `PaymentFailedException` where caught.

## Initialization

`InitializeDB` provisions sample users and a profile/address; `DataInitializer` provisions pizzas if none exist. These are startup behaviors, not an independently documented production data migration strategy.
