package whz.pti.eva.cart.service;

import whz.pti.eva.menu.domain.Pizza;
import whz.pti.eva.menu.domain.PizzaSize;

import java.math.BigDecimal;

public record SessionItemDto(Pizza pizza, int quantity, PizzaSize size) {
    public BigDecimal getPriceForItem() {
        return pizza.getPriceForSize(size).multiply(BigDecimal.valueOf(quantity));
    }
}
