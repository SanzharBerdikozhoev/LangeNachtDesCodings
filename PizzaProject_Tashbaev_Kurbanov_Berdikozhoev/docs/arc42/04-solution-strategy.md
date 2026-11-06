# 4. Solution Strategy

| Concern | Implemented approach | Evidence |
|---|---|---|
| Web interactions | Spring MVC controllers return server-rendered Thymeleaf views | Feature controllers and `src/main/resources/templates/` |
| Separation of responsibilities | Feature packages divide boundary, service, and domain/repository responsibilities | `src/main/java/whz/pti/eva/{cart,customer,menu,ordered,user}/` |
| Persistence | JPA entities and Spring Data repository interfaces | Feature `domain` packages and `spring-boot-starter-data-jpa` in `build.gradle` |
| Authentication | Form login backed by a custom `UserDetailsService`, DAO provider, and password encoder | `auth/SecurityConfig.java`, `user/service/currentuser/CurrentUserDetailsService.java` |
| Guest shopping | In-memory session cart; authenticated cart persisted in database; login handler merges guest items | `cart/domain/session/SessionCart.java`, `cart/service/CartServiceImpl.java`, `auth/LoginSuccessHandler.java` |
| Checkout | Order service calculates item totals, invokes payment service, and persists order data | `ordered/service/OrderedServiceImpl.java` |
| Payment | Configured HTTP client calls payment/account endpoints | `payment/service/PaymentServiceImpl.java`, `application.properties` |

These are implementation strategies observed in source; the repository does not provide formal rationale or an explicit architecture strategy document.
