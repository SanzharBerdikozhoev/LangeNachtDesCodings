package whz.pti.eva.cart.domain.session;

import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;
import whz.pti.eva.menu.domain.PizzaSize;

import java.util.ArrayList;
import java.util.List;

/**
 * SessionCart repräsentiert den Warenkorb eines anonymen Benutzers,
 * der innerhalb einer HTTP-Session (Seesion-Scope) exixtiert und nicht in der Datenbank gespeichert wird.
 */
@Component
@Scope(value = WebApplicationContext.SCOPE_SESSION, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class SessionCart {

    private final List<SessionCartItem> items = new ArrayList<>();

    /**
     * Fügt ein Item zum Session-Warenkorb hinzu.
     * <p>
     * Wenn bereits ein Item mit derselben Pizza-ID und Größe existiert, wird die Menge des vorhandenen Items erhöht.
     * Andernfalls wird ein neues Item erstellt und dem Warenkorb hinzugefügt.
     *
     * @param pizzaId  Die ID der Pizza
     * @param size     Die gewählte Größe
     * @param quantity Die Menge
     */
    public void add(Long pizzaId, PizzaSize size, int quantity) {
        SessionCartItem existingItem = items.stream()
                .filter(i -> i.getPizzaId().equals(pizzaId) && i.getSize() == size)
                .findFirst()
                .orElse(null);

        if (existingItem != null) {
            existingItem.setQuantity(existingItem.getQuantity() + quantity);
        } else {
            items.add(new SessionCartItem(pizzaId, quantity, size));
        }
    }

    /**
     * Entfernt ein Item aus dem Session-Warenkorb.
     *
     * @param idx ID des Items
     */
    public void removeByIndex(Long idx) {
        if (idx < 0 || idx >= items.size()) {
            throw new IllegalArgumentException("Invalide index: " + idx);
        }

        items.remove((int) (long) idx);
    }

    /**
     * Gibt alle Items des Session-Warenkorbs zurück.
     *
     * @return Liste der SessionCartItems
     */
    public List<SessionCartItem> getItems() {
        return items;
    }

    /**
     * Leert den Session-Warenkorb vollständig.
     */
    public void clear() {
        items.clear();
    }

    /**
     * Aktualiesert das Item des Session-Warenkorbs.
     *
     * @param idx       id des zu aktualisierende items
     * @param pizzaSize neue Größe
     * @param quantity  neue Menge
     * @throws IllegalArgumentException, wenn kein Item gefunden ist.
     */
    public void update(Long idx, PizzaSize pizzaSize, int quantity) {
        SessionCartItem item = items.stream()
                .skip(idx)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("SessionCart item mit index " + idx + " ist nicht gefunden"));
        item.setQuantity(quantity);
        item.setSize(pizzaSize);
    }
}
