package whz.pti.eva.menu.domain;


import java.math.BigDecimal;
import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import whz.pti.eva.cart.domain.Item;
import whz.pti.eva.common.domain.BaseEntity;

/**
 * Repräsentiert eine Pizza im Katalog der Pizza-Anwendung.
 * Enthält Namen und Preise für verschiedene Größen sowie Zuordnung zu Bestellpositionen.
 */
@Entity
public class Pizza extends BaseEntity<Long> {

    private String name;

    private BigDecimal priceLarge;

    private BigDecimal priceMedium;

    private BigDecimal priceSmall;

    @OneToMany(mappedBy = "pizza")
    private List<Item> items;

    public Pizza() {}

    public Pizza(String name, BigDecimal priceLarge, BigDecimal priceMedium, BigDecimal priceSmall) {
        this.name = name;
        this.priceLarge = priceLarge;
        this.priceMedium = priceMedium;
        this.priceSmall = priceSmall;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getPriceLarge() {
        return priceLarge;
    }

    public void setPriceLarge(BigDecimal priceLarge) {
        this.priceLarge = priceLarge;
    }

    public BigDecimal getPriceMedium() {
        return priceMedium;
    }

    public void setPriceMedium(BigDecimal priceMedium) {
        this.priceMedium = priceMedium;
    }

    public BigDecimal getPriceSmall() {
        return priceSmall;
    }

    public void setPriceSmall(BigDecimal priceSmall) {
        this.priceSmall = priceSmall;
    }

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

    public BigDecimal getPriceForSize(PizzaSize size) {
        return switch (size) {
            case SMALL -> priceSmall;
            case MEDIUM -> priceMedium;
            case LARGE -> priceLarge;
        };
    }
}
