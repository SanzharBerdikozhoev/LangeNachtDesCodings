# 6. Runtime View

## 6.1 Browse menu

The browser requests the menu route handled by `menu/boundary/PizzaController` (`GET /`). The optional `q` query parameter is passed to `PizzaService.searchByName`; a blank query returns all pizzas and a non-blank query retrieves names containing the trimmed query, case-insensitively. Optional `sort` (`name`, default) and `direction` (`asc`, default) parameters control in-memory ordering of the filtered list: `sort=price` uses `Pizza::getPriceMedium`, otherwise pizzas are sorted by `Pizza::getName` (case-insensitive); `direction=desc` reverses the comparator. The controller adds `pizzas`, `searchQuery`, `sort`, and `direction` to the model and returns `templates/pizza/pizza-menu.html`, where a GET form submits searches and sorting selections. The controller also loads the current user's or guest's cart as described below.

## 6.2 Cart as guest and after login

For an anonymous visitor, `CartServiceImpl` delegates add/update/delete and read operations to the session-scoped `SessionCart`. For authenticated users, cart changes use `CartRepository` and `ItemRepository`. `LoginSuccessHandler` calls `mergeSessionCartIntoDb` after successful authentication, then redirects to the saved login redirect or `/`.

## 6.3 Checkout

`OrderedController` passes selected cart item identifiers and delivery address to `OrderedServiceImpl`. The service loads the user's cart, selects requested items, computes prices from pizza size and quantity, removes the selected cart items, calls `PaymentService.doActionPay`, and saves an `Ordered` record with order items and total. The payment implementation sends an HTTP POST to the configured payment endpoint.

The source shows the sequence above. It does not document distributed transaction semantics between the application database and payment API; the service is annotated `@Transactional`, but that cannot make the remote HTTP call part of a database transaction.

## 6.4 Payment account and deposit

For authenticated `USER` visitors, `templates/pizza/pizza-menu.html` links to `/payment/account` ("Zahlungskonto und Guthaben"). `PaymentController.showAccount` (`GET /payment/account`) requires a current user; if absent it redirects to `/login?redirect=/payment/account` with a flash error. When a user is present, it calls `PaymentService.getBalance` and renders `templates/payment/account.html`; balance-loading failures add an error message to the model.

The account view posts to `/payment/deposit` (`POST`). `PaymentController.doDeposit` requires a current user (otherwise redirects to `/login?redirect=/ordered` with a flash error), validates that the amount is finite and greater than 0 (otherwise redirects back to `/payment/account` with a flash error), then calls `PaymentService.deposit`. Successful deposits and `PaymentFailedException` outcomes are reported via flash messages and redirect to `/payment/account`.

`PaymentServiceImpl` retrieves balances via HTTP GET to the configured payment API account endpoint and performs deposits via HTTP PATCH to the deposit endpoint, both with HTTP Basic credentials derived from configuration. `BalanceControllerAdvice` also adds `currentUser` and balance to model attributes for authenticated requests, using `0.0` as a fallback when balance retrieval fails.

## 6.5 Favorite pizzas

Authenticated `USER` visitors can save favorite pizzas from the menu and manage them on `/favorites`. `templates/pizza/pizza-menu.html` posts to `/favorites/{pizzaId}` (add) and links to `/favorites` ("Meine Favoriten"); the favorite button is disabled when the current user is not a `USER`.

`FavoritePizzaController` (`/favorites`) resolves the current user via `CurrentUserUtils`. `GET /favorites` lists favorites from `FavoritePizzaService.getFavorites(userId)` and renders `templates/favorite/list.html`; if no user is present it redirects to `/login?redirect=/favorites` with a flash error. `POST /favorites/{pizzaId}` adds a favorite (service checks for an existing user/pizza pair, loads the pizza via `PizzaRepository`, and saves a `FavoritePizza`) then redirects to `/favorites` with a success message; unauthenticated access redirects to login. `POST /favorites/{pizzaId}/remove` removes the favorite for the current user when present and redirects to `/favorites`. `auth/SecurityConfig` requires `USER` authority for `/favorites/**`.

## 6.6 Startup initialization

`InitializeDB` creates preset users/customer/address data at `@PostConstruct` when users are absent. `menu/utils/DataInitializer` seeds sample pizzas through `CommandLineRunner` when the pizza repository is empty.
