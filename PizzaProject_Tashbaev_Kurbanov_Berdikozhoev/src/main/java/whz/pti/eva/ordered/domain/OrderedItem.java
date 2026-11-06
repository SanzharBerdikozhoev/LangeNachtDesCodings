package whz.pti.eva.ordered.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import whz.pti.eva.common.domain.BaseEntity;
import whz.pti.eva.menu.domain.PizzaSize;

import java.math.BigDecimal;

/**
 * Entity-Klasse für eine einzelne Bestellposition innerhalb einer Bestellung.
 * Enthält Informationen über die Pizza, Menge, Preis, Benutzer und Größe.
 */
@Entity
public class OrderedItem extends BaseEntity<Long> {

    /**
     * ID der Pizza, die bestellt wurde.
     */
    private Long pizzaId;

    /**
     * Name der Pizza.
     */
    private String name;

    /**
     * Menge dieser Pizza in der Bestellung.
     */
    private int quantity;

    /**
     * Preis für diese Bestellposition (Menge * Preis pro Einheit).
     */
    private BigDecimal price;

    /**
     * ID des Benutzers, der die Bestellung aufgegeben hat.
     */
    private String userId;

    /**
     * Größe der Pizza (SMALL, MEDIUM, LARGE).
     */
    @Enumerated(EnumType.STRING)
    private PizzaSize size;

    /**
     * Konstruktor zum Erstellen eines OrderedItem mit allen wichtigen Informationen.
     *
     * @param pizzaId ID der Pizza
     * @param name Name der Pizza
     * @param quantity Menge der Pizza
     * @param price Preis für diese Bestellposition
     * @param userId ID des Benutzers
     * @param size Größe der Pizza
     */
    public OrderedItem(Long pizzaId, String name, int quantity, BigDecimal price, String userId, PizzaSize size) {
        this.pizzaId = pizzaId;
        this.name = name;
        this.quantity = quantity;
        this.price = price;
        this.userId = userId;
        this.size = size;
    }

    /**
     * Standard-Konstruktor.
     */
    public OrderedItem() {}

    /**
     * Gibt den Namen der Pizza zurück.
     *
     * @return Name der Pizza
     */
    public String getName() {
        return name;
    }

    /**
     * Setzt den Namen der Pizza.
     *
     * @param name Name der Pizza
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gibt die Menge der Pizza in dieser Bestellposition zurück.
     *
     * @return Menge der Pizza
     */
    public int getQuantity() {
        return quantity;
    }

    /**
     * Setzt die Menge der Pizza in dieser Bestellposition.
     *
     * @param quantity Menge der Pizza
     */
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    /**
     * Gibt die Größe der Pizza zurück.
     *
     * @return Pizza-Größe
     */
    public PizzaSize getSize() {
        return size;
    }

    /**
     * Setzt die Größe der Pizza.
     *
     * @param size Pizza-Größe
     */
    public void setSize(PizzaSize size) {
        this.size = size;
    }

    /**
     * Gibt den Preis der Bestellposition zurück.
     *
     * @return Preis der Bestellposition
     */
    public BigDecimal getPrice() {
        return price;
    }

    /**
     * Setzt den Preis der Bestellposition.
     *
     * @param price Preis der Bestellposition
     */
    public void setPrice(BigDecimal price) {
        this.price = price;
    }
}
