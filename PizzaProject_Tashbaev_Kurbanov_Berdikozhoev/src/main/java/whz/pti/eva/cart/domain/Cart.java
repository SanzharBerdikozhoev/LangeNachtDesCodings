package whz.pti.eva.cart.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import whz.pti.eva.common.domain.BaseEntity;
import whz.pti.eva.customer.domain.Customer;

import java.util.ArrayList;
import java.util.List;

/**
 * Cart repräsentiert den persistenten Warenkorb eines registrierten Benutzers.
 */
@Entity
public class Cart extends BaseEntity<Long> {

    private Integer quantity = 0;

    private String userId;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Item> items = new ArrayList<>();

    @OneToOne
    private Customer customer;

    public Cart(String userId) {
        this.userId = userId;
    }

    public Cart() {
    }

    /**
     * Fügt ein Item hinzu und aktualisiert die Gesamtmenge.
     */
    public void addItem(Item item) {
        items.add(item);
        item.setCart(this);
        recalculateQuantity();
    }

    /**
     * Entfernt ein Item aus dem Warenkorb.
     */
    public void removeItem(Item item) {
        items.remove(item);
        item.setCart(null);
        recalculateQuantity();
    }

    /**
     * Berechnet die Gesamtanzahl aller Items im Warenkorb.
     */
    public void recalculateQuantity() {
        this.quantity = items.stream().mapToInt(Item::getQuantity).sum();
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }
}
