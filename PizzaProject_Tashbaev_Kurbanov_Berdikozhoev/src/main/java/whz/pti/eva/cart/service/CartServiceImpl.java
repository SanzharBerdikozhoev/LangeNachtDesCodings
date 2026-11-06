package whz.pti.eva.cart.service;

import jakarta.transaction.Transactional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import whz.pti.eva.cart.domain.Cart;
import whz.pti.eva.cart.domain.CartRepository;
import whz.pti.eva.cart.domain.Item;
import whz.pti.eva.cart.domain.ItemRepository;
import whz.pti.eva.cart.domain.session.SessionCart;
import whz.pti.eva.cart.domain.session.SessionCartItem;
import whz.pti.eva.common.exceptions.ItemNotFoundException;
import whz.pti.eva.common.exceptions.PizzaNotFoundException;
import whz.pti.eva.menu.domain.Pizza;
import whz.pti.eva.menu.domain.PizzaRepository;
import whz.pti.eva.user.domain.User;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Service-Implementierung für den Warenkorb.
 * Verwaltet das Hinzufügen, Aktualisieren, Löschen und Abrufen von Items im Warenkorb.
 */
@Service
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final ItemRepository itemRepository;
    private final PizzaRepository pizzaRepository;
    private final SessionCart sessionCart;

    /**
     * Konstruktor für die CartServiceImpl, der benötigten Repositories injiziert.
     *
     * @param cartRepository  Repository für den Warenkorb
     * @param itemRepository  Repository für Items im Warenkorb
     * @param pizzaRepository Repository für Pizzas
     * @param sessionCart     Session-Warenkorb für anonyme Nutzer
     */
    public CartServiceImpl(CartRepository cartRepository, ItemRepository itemRepository, PizzaRepository pizzaRepository, SessionCart sessionCart) {
        this.cartRepository = cartRepository;
        this.itemRepository = itemRepository;
        this.pizzaRepository = pizzaRepository;
        this.sessionCart = sessionCart;
    }

    /**
     * Fügt ein Item für einen bestimmten Benutzer zum Warenkorb hinzu.
     *
     * @param itemRequestDto DTO mit Pizza, Größe und Menge
     * @throws PizzaNotFoundException wenn die Pizza nicht existiert
     */
    @Override
    @Transactional
    public void addItem(ItemRequestDto itemRequestDto) {
        if (isLoggedIn()) {
            Cart cart = getOrCreateDbCart(getCurrentUserId());
            addToDbCart(cart, itemRequestDto);
        } else {
            sessionCart.add(itemRequestDto.pizzaId(), itemRequestDto.pizzaSize(), itemRequestDto.quantity());
        }
    }

    /**
     * Fügt ein Item in den persistenten Warenkorb (Datenbank) hinzu.
     * <p>
     * Wenn ein Item mit derselben Pizza und Größe bereits existiert, wird die Menge des vorhandenen Items erhöht.
     * Andernfalls wird ein neues Item erstellt, dem Warenkorb hinzugefügt und gespeichert.
     *
     * @param cart Der Warenkorb (Entity), zu dem das Item hinzugefügt werden soll.
     * @param dto  Das DTO mit den Details des Items (Pizza-ID, Größe, Menge).
     * @throws PizzaNotFoundException wenn die Pizza mit der angegebenen ID nicht in der Datenbank gefunden wird.
     */
    private void addToDbCart(Cart cart, ItemRequestDto dto) {
        Pizza pizza = pizzaRepository.findById(dto.pizzaId())
                .orElseThrow(() -> new PizzaNotFoundException(dto.pizzaId()));

        Item existingItem = cart.getItems().stream()
                .filter(i -> i.getPizza().getId().equals(pizza.getId()) &&
                        i.getSize() == dto.pizzaSize())
                .findFirst()
                .orElse(null);

        if (existingItem != null) {
            existingItem.setQuantity(existingItem.getQuantity() + dto.quantity());
            itemRepository.save(existingItem);
        } else {
            Item item = new Item(dto.quantity(), dto.pizzaSize(), pizza, cart);
            cart.addItem(item);
            itemRepository.save(item);
        }

        cartRepository.save(cart);
    }

    /**
     * Verschmilzt den Session-Warenkorb eines Nutzers in den DB-Warenkorb nach Login.
     *
     * @param userId ID des eingeloggten Nutzers
     */
    @Override
    @Transactional
    public void mergeSessionCartIntoDb(String userId) {
        Cart cart = getOrCreateDbCart(userId);

        for (SessionCartItem sessionItem : sessionCart.getItems()) {
            Pizza pizza = pizzaRepository.findById(sessionItem.getPizzaId())
                    .orElseThrow();

            Item item = new Item(
                    sessionItem.getQuantity(),
                    sessionItem.getSize(),
                    pizza,
                    cart);

            cart.addItem(item);
        }

        cartRepository.save(cart);
        sessionCart.clear();
    }

    /**
     * Prüft, ob aktuell ein Benutzer eingeloggt ist.
     *
     * @return true, wenn ein Benutzer eingeloggt ist, sonst false
     */
    @Override
    public boolean isLoggedIn() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal());
    }

    /**
     * Liefert den DB-Warenkorb des aktuell eingeloggten Benutzers.
     *
     * @return Warenkorb oder null, falls kein Warenkorb vorhanden
     */
    @Override
    public Cart getDBCart() {
        String userId = getCurrentUserId();
        return cartRepository.findByUserIdWithItems(userId).orElse(null);
    }

    /**
     * Wandelt Session-Cart-Indizes in DB-Item-IDs um und speichert sie in der Datenbank.
     *
     * @param sessionIndexes Liste der Session-Item-Indizes
     * @param userId         ID des eingeloggten Benutzers
     * @return Liste der neuen DB-Item-IDs
     */
    @Override
    public List<Long> mapSessionIndexesToDbIds(List<Long> sessionIndexes, String userId) {
        Cart cart = getOrCreateDbCart(getCurrentUserId());
        List<SessionCartItem> sessionCartItems = sessionCart.getItems();

        List<Long> dbItemIds = new ArrayList<>();

        for (Long idx : sessionIndexes) {
            if (idx < 0 || idx >= sessionCartItems.size()) continue;

            SessionCartItem scItem = sessionCartItems.get(Math.toIntExact(idx));
            Pizza pizza = pizzaRepository.findById(scItem.getPizzaId())
                    .orElseThrow(() -> new PizzaNotFoundException(scItem.getPizzaId()));

            Item dbItem = new Item(scItem.getQuantity(), scItem.getSize(), pizza, cart);

            itemRepository.save(dbItem);
            dbItemIds.add(dbItem.getId());
        }

        cartRepository.save(cart);
        sessionCart.clear();

        return dbItemIds;
    }


    /**
     * Liefert alle Items aus dem Session-Warenkorb.
     *
     * @return Liste der SessionCartItems
     */
    @Override
    public List<SessionCartItem> getSessionItems() {
        return sessionCart.getItems();
    }

    /**
     * Aktualisiert ein vorhandenes Item im Warenkorb.
     *
     * @param itemId         ID des DB-Items
     * @param itemRequestDto DTO mit neuer Größe und Menge
     * @throws ItemNotFoundException falls das Item nicht existiert
     */
    @Override
    @Transactional
    public void updateItem(Long itemId, ItemRequestDto itemRequestDto) throws ItemNotFoundException {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ItemNotFoundException("Item mit id " + itemId + " wurde nicht gefunden."));

        item.setQuantity(itemRequestDto.quantity());
        item.setSize(itemRequestDto.pizzaSize());

        Cart cart = item.getCart();
        if (cart != null) {
            cart.recalculateQuantity();
            cartRepository.save(cart);
        }

        itemRepository.save(item);
    }

    /**
     * Aktualisiert ein Item im Session-Warenkorb.
     *
     * @param itemIdx        Index des Session-Items
     * @param itemRequestDto DTO mit neuer Größe und Menge
     */
    @Override
    public void updateSessionItem(Long itemIdx, ItemRequestDto itemRequestDto) {
        sessionCart.update(itemIdx, itemRequestDto.pizzaSize(), itemRequestDto.quantity());
    }

    /**
     * Entfernt ein DB-Item aus dem Warenkorb.
     *
     * @param itemId ID des zu löschenden Items
     * @throws ItemNotFoundException wenn das Item nicht gefunden wird
     */
    @Override
    @Transactional
    public void deleteItem(Long itemId) throws ItemNotFoundException {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ItemNotFoundException("Item mit id " + itemId + " wurde nicht gefunden."));

        Cart cart = item.getCart();
        if (cart != null) {
            cart.removeItem(item);
            cartRepository.save(cart);
        }
    }

    /**
     * Entfernt ein Item aus dem Session-Warenkorb.
     *
     * @param itemIdx Index des zu löschenden Session-Items
     */
    @Override
    public void deleteSessionItem(Long itemIdx) {
        sessionCart.removeByIndex(itemIdx);
    }

    /**
     * Liefert den Warenkorb eines bestimmten Benutzers oder gibt null züruck, falls keiner existiert.
     *
     * @param userId Benutzer-ID
     * @return Warenkorb des Benutzers
     */
    @Override
    public Cart findByUserId(String userId) {
        return cartRepository.findByUserIdWithItems(userId).orElse(null);
    }

    /**
     * Liefert die ID des aktuell eingeloggten Benutzers.
     *
     * @return Benutzer-ID oder null, wenn kein Benutzer eingeloggt ist
     */
    @Override
    public String getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            return auth.getName();
        }

        return null;
    }

    /**
     * Hilfsmethode: Liefert den DB-Warenkorb eines Benutzers oder erstellt einen neuen, falls nicht vorhanden.
     *
     * @param userId Benutzer-ID
     * @return DB-Warenkorb
     */
    private Cart getOrCreateDbCart(String userId) {
        return cartRepository.findByUserIdWithItems(String.valueOf(userId))
                .orElseGet(() -> cartRepository.save(new Cart(String.valueOf(userId))));
    }

    /**
     * Berechnet die Preise für eine gegebene Liste von Warenkorb-Items.
     * Ermittelt den Preis basierend auf der Pizzagröße und der Menge für jedes Item.
     *
     * @param items Die Liste der Items, für die die Preise berechnet werden sollen.
     * @return Eine Liste von BigDecimal-Werten, die die berechneten Preise für die Items repräsentieren.
     */
    @Override
    public List<BigDecimal> calculatePricesForItems(List<Item> items) {
        return items.stream()
                .map(item -> item.getPizza()
                        .getPriceForSize(item.getSize())
                        .multiply(BigDecimal.valueOf(item.getQuantity()))).toList();
    }

    /**
     * Wandelt eine Liste von Session-Warenkorb-Items in entsprechende DTOs (Data Transfer Objects) um.
     * Dies wird verwendet, um interne Session-Daten in ein Format zu konvertieren, das für die Anzeige in der UI geeignet ist.
     *
     * @param sessionItems Die Liste der Items aus dem Session-Warenkorb.
     * @return Eine Liste von SessionItemDto-Objekten.
     */
    @Override
    public List<SessionItemDto> mapSessionItemsToSessionItemDto(List<SessionCartItem> sessionItems) {
        return sessionItems.stream()
                .map(item -> new SessionItemDto(
                        pizzaRepository.findById(item.getPizzaId()).orElseThrow(),
                        item.getQuantity(),
                        item.getSize()
                ))
                .toList();
    }
}
