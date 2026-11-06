package whz.pti.eva.cart.service;

import jakarta.transaction.Transactional;
import whz.pti.eva.cart.domain.Cart;
import whz.pti.eva.cart.domain.Item;
import whz.pti.eva.cart.domain.session.SessionCartItem;
import whz.pti.eva.common.exceptions.ItemNotFoundException;
import whz.pti.eva.common.exceptions.PizzaNotFoundException;

import java.math.BigDecimal;
import java.util.List;

/**
 * Service-Interface für den Warenkorb.
 * Definiert grundlegende Operationen für Items und Warenkorbverwaltung.
 */
public interface CartService {

    /**
     * Fügt ein Item für einen bestimmten Benutzer zum Warenkorb hinzu.
     *
     * @param itemRequestDto DTO mit Pizza, Größe und Menge
     * @throws PizzaNotFoundException falls die Pizza nicht gefunden wird
     */
    @Transactional
    void addItem(ItemRequestDto itemRequestDto) throws PizzaNotFoundException;

    /**
     * Verschmilzt den Session-Warenkorb eines Benutzers mit dessen DB-Warenkorb nach Login.
     *
     * @param userId ID des eingeloggten Benutzers
     */
    @Transactional
    void mergeSessionCartIntoDb(String userId);

    /**
     * Prüft, ob ein Benutzer aktuell eingeloggt ist.
     *
     * @return true, falls ein Benutzer eingeloggt ist, sonst false
     */
    boolean isLoggedIn();

    /**
     * Liefert den Warenkorb eines eingeloggten Benutzers aus der Datenbank.
     *
     * @return Warenkorb des Benutzers
     */
    Cart getDBCart();

    /**
     * Wandelt Session-Item-Indizes in DB-Item-IDs für den Benutzer um.
     *
     * @param sessionIndexes Liste der Session-Item-Indizes
     * @param userId         Benutzer-ID
     * @return Liste der entsprechenden DB-Item-IDs
     */
    List<Long> mapSessionIndexesToDbIds(List<Long> sessionIndexes, String userId);

    /**
     * Liefert die Items aus dem Session-Warenkorb.
     *
     * @return Liste der SessionCartItems
     */
    List<SessionCartItem> getSessionItems();

    /**
     * Aktualisiert ein vorhandenes Item im Warenkorb.
     *
     * @param itemId         ID des zu aktualisierenden Items
     * @param itemRequestDto DTO mit neuen Werten für Menge und Größe
     * @throws ItemNotFoundException falls das Item nicht gefunden wird
     */
    void updateItem(Long itemId, ItemRequestDto itemRequestDto) throws ItemNotFoundException;

    /**
     * Aktualisiert ein Item im Session-Warenkorb.
     *
     * @param itemIdx        Index des Items im Session-Warenkorb
     * @param itemRequestDto DTO mit neuen Werten für Menge und Größe
     */
    void updateSessionItem(Long itemIdx, ItemRequestDto itemRequestDto);

    /**
     * Entfernt ein Item aus dem Warenkorb.
     *
     * @param itemId ID des zu löschenden Items
     * @throws ItemNotFoundException falls das Item nicht gefunden wird
     */
    void deleteItem(Long itemId) throws ItemNotFoundException;

    /**
     * Entfernt ein Item aus dem Session-Warenkorb.
     *
     * @param itemIdx Index des Items im Session-Warenkorb
     */
    void deleteSessionItem(Long itemIdx);

    /**
     * Findet oder erstellt einen Warenkorb für einen bestimmten Benutzer.
     *
     * @param userId Benutzer-ID
     * @return Warenkorb für den Benutzer
     */
    Cart findByUserId(String userId);

    /**
     * Liefert die ID des aktuell eingeloggten Benutzers.
     *
     * @return Benutzer-ID oder null, falls kein Benutzer eingeloggt ist
     */
    String getCurrentUserId();

    /**
     * Berechnet die Preise für eine gegebene Liste von Warenkorb-Items.
     * Ermittelt den Preis basierend auf der Pizzagröße und der Menge für jedes Item.
     *
     * @param items Die Liste der Items, für die die Preise berechnet werden sollen.
     * @return Eine Liste von BigDecimal-Werten, die die berechneten Preise für die Items repräsentieren.
     */
    List<BigDecimal> calculatePricesForItems(List<Item> items);

    /**
     * Wandelt eine Liste von Session-Warenkorb-Items in entsprechende DTOs (Data Transfer Objects) um.
     * Dies wird verwendet, um interne Session-Daten in ein Format zu konvertieren, das für die Anzeige in der UI geeignet ist.
     *
     * @param sessionItems Die Liste der Items aus dem Session-Warenkorb.
     * @return Eine Liste von SessionItemDto-Objekten.
     */
    List<SessionItemDto> mapSessionItemsToSessionItemDto(List<SessionCartItem> sessionItems);
}
