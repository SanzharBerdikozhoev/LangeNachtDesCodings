package whz.pti.eva.ordered.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import whz.pti.eva.common.domain.BaseEntity;

import java.math.BigDecimal;
import java.util.List;

/**
 * Entity-Klasse für eine Bestellung (Ordered) eines Benutzers.
 * Enthält Informationen über die Bestellpositionen, Gesamtpreis und Anzahl der Positionen.
 */
@Entity
public class Ordered extends BaseEntity<Long> {
    /**
     * Anzahl der Bestellpositionen in dieser Bestellung.
     */
    private int numberOfItems;

    /**
     * ID des Benutzers, der die Bestellung aufgegeben hat.
     */
    private String userId;

    /**
     * Gesamtpreis der Bestellung.
     */
    private BigDecimal totalPrice;

    /**
     * ID der Address wohin Bestellung geliefert muss
     */
    private Long deliveryAddressId;

    public Long getDeliveryAddressId() {
        return deliveryAddressId;
    }

    public void setDeliveryAddressId(Long deliveryAddressId) {
        this.deliveryAddressId = deliveryAddressId;
    }

    /**
     * Liste der einzelnen Bestellpositionen (OrderedItem).
     * CascadeType.ALL sorgt dafür, dass Änderungen an Ordered auf die OrderedItems durchgereicht werden.
     */
    @OneToMany(cascade = CascadeType.ALL)
    private List<OrderedItem> items;

    /**
     * Standard-Konstruktor.
     */
    public Ordered() {}

    /**
     * Konstruktor zum Erstellen einer Bestellung für einen bestimmten Benutzer.
     *
     * @param userId ID des Benutzers
     */
    public Ordered(String userId) {
        this.userId = userId;
    }

    /**
     * Gibt den Gesamtpreis der Bestellung zurück.
     *
     * @return Gesamtpreis
     */
    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    /**
     * Setzt den Gesamtpreis der Bestellung.
     *
     * @param totalPrice Gesamtpreis
     */
    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    /**
     * Gibt die Anzahl der Bestellpositionen zurück.
     *
     * @return Anzahl der Items
     */
    public int getNumberOfItems() {
        return numberOfItems;
    }

    /**
     * Setzt die Anzahl der Bestellpositionen.
     *
     * @param numberOfItems Anzahl der Items
     */
    public void setNumberOfItems(int numberOfItems) {
        this.numberOfItems = numberOfItems;
    }

    /**
     * Gibt die ID des Benutzers zurück.
     *
     * @return Benutzer-ID
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Setzt die ID des Benutzers.
     *
     * @param userId Benutzer-ID
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * Gibt die Liste der Bestellpositionen zurück.
     *
     * @return Liste von OrderedItem
     */
    public List<OrderedItem> getItems() {
        return items;
    }

    /**
     * Setzt die Liste der Bestellpositionen.
     *
     * @param items Liste von OrderedItem
     */
    public void setItems(List<OrderedItem> items) {
        this.items = items;
    }
}
