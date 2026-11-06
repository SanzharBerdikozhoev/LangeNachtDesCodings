package whz.pti.eva.cart.service;

import whz.pti.eva.menu.domain.PizzaSize;


public record ItemRequestDto(Long pizzaId, PizzaSize pizzaSize, int quantity) {
}
