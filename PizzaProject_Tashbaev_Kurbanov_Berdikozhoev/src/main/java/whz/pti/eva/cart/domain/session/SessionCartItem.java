package whz.pti.eva.cart.domain.session;

import whz.pti.eva.menu.domain.PizzaSize;

/**
 * Repräsentiert ein einzelnes Item im Session-Warenkorb.
 */
public class SessionCartItem {

    private Long pizzaId;

    private int quantity;

    private PizzaSize size;

    public SessionCartItem(Long pizzaId, int quantity, PizzaSize size) {
        this.pizzaId = pizzaId;
        this.quantity = quantity;
        this.size = size;
    }

    public Long getPizzaId() {
        return pizzaId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public PizzaSize getSize() {
        return size;
    }

    public void setSize(PizzaSize size) {
        this.size = size;
    }
}
