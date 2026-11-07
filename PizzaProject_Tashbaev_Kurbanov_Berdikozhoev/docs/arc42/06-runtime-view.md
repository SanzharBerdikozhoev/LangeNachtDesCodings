# 6. Runtime View

## 6.1 Browse menu

The browser requests the menu route handled by `menu/boundary/PizzaController`. The optional `q` query parameter is passed to `PizzaService.searchByName`; a blank query returns all pizzas and a non-blank query retrieves names containing the trimmed query, case-insensitively. Optional `sort` and `direction` parameters select name or medium-price sorting and ascending or descending order. The controller returns the filtered, sorted list to `templates/pizza/pizza-menu.html`, where a GET form submits searches and sorting selections. The controller also loads the current user's or guest's cart as described below.

## 6.2 Cart as guest and after login

For an anonymous visitor, `CartServiceImpl` delegates add/update/delete and read operations to the session-scoped `SessionCart`. For authenticated users, cart changes use `CartRepository` and `ItemRepository`. `LoginSuccessHandler` calls `mergeSessionCartIntoDb` after successful authentication, then redirects to the saved login redirect or `/`.

## 6.3 Checkout

`OrderedController` passes selected cart item identifiers and delivery address to `OrderedServiceImpl`. The service loads the user's cart, selects requested items, computes prices from pizza size and quantity, removes the selected cart items, calls `PaymentService.doActionPay`, and saves an `Ordered` record with order items and total. The payment implementation sends an HTTP POST to the configured payment endpoint.

The source shows the sequence above. It does not document distributed transaction semantics between the application database and payment API; the service is annotated `@Transactional`, but that cannot make the remote HTTP call part of a database transaction.

## 6.4 Startup initialization

`InitializeDB` creates preset users/customer/address data at `@PostConstruct` when users are absent. `menu/utils/DataInitializer` seeds sample pizzas through `CommandLineRunner` when the pizza repository is empty.
