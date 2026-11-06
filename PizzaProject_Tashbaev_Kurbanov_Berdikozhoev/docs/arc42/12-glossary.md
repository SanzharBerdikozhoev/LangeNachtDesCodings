# 12. Glossary

| Term | Meaning in this project |
|---|---|
| Pizza | Menu entity with name and separate small, medium, and large prices (`menu/domain/Pizza.java`). |
| Cart | Shopping container; persisted for authenticated users or held in session for guests. |
| Cart item (`Item`) | A pizza selection with size and quantity, associated with a cart and pizza. |
| Session cart | Guest cart state represented by `SessionCart` and `SessionCartItem`. |
| Order (`Ordered`) | Persisted checkout record containing order items, user identifier, delivery-address identifier, item count, and total price. |
| Order item (`OrderedItem`) | Snapshot-like order line containing pizza identification/name, quantity, price, user identifier, and size. |
| Customer | Profile data associated with a `User`; also related to delivery addresses. |
| Delivery address | Address entity associated with customers and referenced by an order identifier. |
| User / role | Login account and its `Role` value used by Spring Security authorization. |
| Payment API | External HTTP service called by `PaymentServiceImpl`; its implementation/provider is not part of this repository. |
| Boundary | Package naming used for MVC controllers in feature modules. |
