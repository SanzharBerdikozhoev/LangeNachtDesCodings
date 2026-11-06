package whz.pti.eva.cart.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;

import jakarta.persistence.ManyToOne;
import whz.pti.eva.common.domain.BaseEntity;
import whz.pti.eva.menu.domain.Pizza;
import whz.pti.eva.menu.domain.PizzaSize;

/**
 * Item repräsentiert eine einzelne Position im persistenten Warenkorb.
 */
@Entity
public class Item extends BaseEntity<Long> {

    private Integer quantity = 0;

    @Enumerated(EnumType.STRING)
    private PizzaSize size;

    @ManyToOne(fetch = FetchType.LAZY)
    private Pizza pizza;

    @ManyToOne(fetch = FetchType.LAZY)
    private Cart cart;

    public Item() {
    }

    public Item(Integer quantity, PizzaSize size, Pizza pizza, Cart cart) {
        this.quantity = quantity;
        this.size = size;
        this.pizza = pizza;
        this.cart = cart;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public PizzaSize getSize() {
        return size;
    }

    public void setSize(PizzaSize size) {
        this.size = size;
    }

    public Pizza getPizza() {
        return pizza;
    }

    public void setPizza(Pizza pizza) {
        this.pizza = pizza;
    }

    public Cart getCart() {
        return cart;
    }

    public void setCart(Cart cart) {
        this.cart = cart;
    }
}
