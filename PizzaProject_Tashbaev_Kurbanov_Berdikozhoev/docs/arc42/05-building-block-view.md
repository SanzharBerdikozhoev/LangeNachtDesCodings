# 5. Building Block View

## 5.1 Level 1: application

The Spring Boot application starts from `PizzaProjectTashbaevKurbanovBerdikozhoevApplication`. Its implementation is organized as a modular monolith in one Gradle project and one application package tree.

## 5.2 Level 2: feature modules

| Module | Responsibility evidenced by source |
|---|---|
| `menu` | Pizza entity/repository, menu service/controller, and pizza seed data. |
| `cart` | Cart and item persistence, session cart, cart service, and web controller. |
| `favorite` | Favorite-pizza entity/repository, service, and controller for authenticated users to save, list, and remove favorite pizzas; uses pizza-menu and `templates/favorite/list.html`. |
| `ordered` | Order and order-item persistence, order service/controller, order DTOs. |
| `customer` | Customer profile and delivery address entities, repositories, services, and controllers. |
| `user` | User/role entities, registration/login endpoints, current-user details, and user service. |
| `auth` | Spring Security configuration and successful-login cart merge/redirect handler. |
| `payment` | Payment service, DTOs, controller, `BalanceControllerAdvice`, and payment/account templates. `PaymentController` serves the authenticated account view (`GET /payment/account`, `templates/payment/account.html`) and deposit action (`POST /payment/deposit`); `BalanceControllerAdvice` adds `currentUser` and payment balance to model data for authenticated requests. `PaymentServiceImpl` calls the configured external payment API for account/balance, deposit, and checkout payment operations. |
| `common` | Shared base entity, database initialization, session utility, and domain exceptions. |

Most features use `boundary` for MVC endpoints, `service` for business logic, and `domain` for entities/repositories. This is a package convention, not a separate runtime service boundary.

## 5.3 Persistence model (high-level)

JPA entities include `Pizza`, `Cart`, `Item`, `FavoritePizza`, `User`, `Customer`, `DeliveryAddress`, `Ordered`, and `OrderedItem`. `Item` references a pizza and cart; `Cart` contains items and is associated with a user identifier. `FavoritePizza` stores a `userId` and references a `Pizza`, with a unique constraint on `(user_id, pizza_id)`. `User` has a one-to-one customer relationship; customers and delivery addresses are many-to-many. An order contains order items and stores user and delivery-address identifiers. See the entity classes in each module's `domain` package for exact mappings.
