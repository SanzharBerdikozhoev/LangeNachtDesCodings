# 1. Introduction and Goals

## 1.1 Purpose

The repository implements a web application for browsing a pizza menu, maintaining a cart, managing customer profiles and delivery addresses, and placing orders. The project description in `build.gradle` calls it a Spring Boot pizza project; detailed business requirements or stakeholder goals are not present in the inspected repository.

## 1.2 Main capabilities evidenced by the implementation

- Browse pizza menu and administer pizza entries (`menu/boundary/PizzaController.java`).
- Register and authenticate users (`user/boundary/UserController.java`, `auth/SecurityConfig.java`).
- Maintain customer profiles and delivery addresses (`customer/boundary/`).
- Manage anonymous and authenticated carts (`cart/service/CartServiceImpl.java`).
- Place and view orders (`ordered/service/OrderedServiceImpl.java`).
- Interact with an external payment HTTP API (`payment/service/PaymentServiceImpl.java`).

## 1.3 Quality goals

No explicit quality goals, measurable targets, or stakeholder priorities are documented in the repository. See chapter 10 for qualities that can be observed from implementation and remaining unknowns.
