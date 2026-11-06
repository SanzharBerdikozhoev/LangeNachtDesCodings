package whz.pti.eva.cart.boundary;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import whz.pti.eva.cart.domain.Cart;
import whz.pti.eva.cart.service.ItemRequestDto;
import whz.pti.eva.cart.domain.session.SessionCartItem;
import whz.pti.eva.cart.service.CartService;
import whz.pti.eva.cart.service.SessionItemDto;
import whz.pti.eva.customer.domain.*;
import whz.pti.eva.customer.service.CustomerService;
import whz.pti.eva.menu.domain.PizzaSize;
import whz.pti.eva.user.domain.CurrentUser;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller für Warenkorb-Operationen.
 * Verwaltet das Hinzufügen, Aktualisieren, Löschen und Anzeigen von Items im Warenkorb.
 */
@Controller
@RequestMapping("/cart")
public class CartController {

    private final CartService cartService;
    private final CustomerService customerService;

    /**
     * Konstruktor für den {@link CartController} injiziert folgende Abhängigkeiten.
     *
     * @param cartService     Service für die Verwaltung von Warenkorb-Operationen
     * @param customerService Service für die Verwaltung von Kunden
     */
    public CartController(CartService cartService, CustomerService customerService) {
        this.cartService = cartService;
        this.customerService = customerService;
    }

    /**
     * Zeigt den Warenkorb eines bestimmten oder auch Anonymen Benutzers an.
     *
     * @param model Model für View-Attribute
     * @return Name der View für die Warenkorb-Seite
     */
    @GetMapping("")
    public String viewCart(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        boolean authenticated = auth != null &&
                auth.isAuthenticated() &&
                !(auth instanceof AnonymousAuthenticationToken);

        BigDecimal cartTotalPrice;

        if (authenticated) {
            CurrentUser currentUser = (CurrentUser) auth.getPrincipal();
            Cart currentCart = cartService.getDBCart();
            model.addAttribute("cart", currentCart);

            Customer currentCustomer = currentUser.getUser().getCustomer();

            if (currentCustomer != null) {
                model.addAttribute("form", new DeliveryAddressForm());
                List<DeliveryAddress> addresses = customerService.getAddressesByCustomer(currentCustomer);
                model.addAttribute("deliveryAddresses", addresses);
            } else {
                model.addAttribute("form", new ProfileAndAddressForm());
            }

            List<BigDecimal> itemsPrices = new ArrayList<>(cartService.calculatePricesForItems(currentCart.getItems()));
            cartTotalPrice = itemsPrices.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
            model.addAttribute("itemPrices", itemsPrices);

        } else {
            List<SessionCartItem> sessionCartItems = cartService.getSessionItems();
            List<SessionItemDto> items = cartService.mapSessionItemsToSessionItemDto(sessionCartItems);
            List<BigDecimal> itemPrices = items.stream().map(SessionItemDto::getPriceForItem).toList();
            cartTotalPrice = itemPrices.stream().reduce(BigDecimal.ZERO, BigDecimal::add);

            model.addAttribute("sessionCart", items);
        }

        model.addAttribute("cartTotalPrice", cartTotalPrice);
        return "cart/cart-list";
    }

    /**
     * Fügt eine Pizza zum Warenkorb hinzu.
     * <p>
     * Der Benutzer kann entweder eingeloggt oder anonym sein. Der CartService
     * entscheidet anhand der Request-Daten, welchem Warenkorb das Item hinzugefügt wird.
     *
     * @param pizzaId            ID der zu bestellenden Pizza
     * @param size               gewählte Pizza-Größe (SMALL, MEDIUM, LARGE)
     * @param quantity           Menge der Pizza (Standard: 1)
     * @param redirectAttributes Flash-Attribute für Erfolgsmeldungen
     * @return Redirect auf die Startseite nach dem Hinzufügen
     */
    @PostMapping("/add")
    public String addToCart(@RequestParam("pizzaId") Long pizzaId,
                            @RequestParam("size") String size,
                            @RequestParam(value = "quantity", defaultValue = "1") int quantity,
                            RedirectAttributes redirectAttributes) {
        ItemRequestDto itemRequestDto = new ItemRequestDto(pizzaId, PizzaSize.valueOf(size), quantity);
        cartService.addItem(itemRequestDto);

        redirectAttributes.addFlashAttribute("message", "In den Warenkorb hinzugefügt!");
        return "redirect:/";
    }

    /**
     * Löscht ein Item aus dem Warenkorb des eingeloggten Benutzers.
     *
     * @param itemId ID des zu löschenden Items
     * @return Weiterleitung zur Warenkorb-Seite
     */
    @PostMapping("/delete/{itemId}")
    public String deleteItem(@PathVariable("itemId") Long itemId) {
        cartService.deleteItem(itemId);
        return "redirect:/cart";
    }

    /**
     * Löscht ein Item aus dem Warenkorb des anonymen Benutzers.
     * <p>
     * Unterscheidet sich mit deleteItem, da es Unterschied zwischen realen DB-Cart und Session-Cart gibt
     *
     * @param itemIdx ID des zu löschenden Items
     * @return Weiterleitung zur Warenkorb-Seite
     */
    @PostMapping("/delete/session/{itemIdx}")
    public String deleteSessionItem(@PathVariable("itemIdx") Long itemIdx) {
        cartService.deleteSessionItem(itemIdx);
        return "redirect:/cart";
    }

    /**
     * Aktualisiert ein Item im Warenkorb des eingeloggten Benutzers.
     * Die Pizza-ID wird hier nicht geändert, daher wird im ItemRequestDto der Wert {@code null} übergeben.
     *
     * @param itemId   ID des zu aktualisierenden Items
     * @param quantity Neue Menge
     * @param size     Neue Größe
     * @return Weiterleitung zur Warenkorb-Seite
     */
    @PostMapping("/update/{itemId}")
    public String updateItem(@PathVariable("itemId") Long itemId, @RequestParam("quantity") int quantity, @RequestParam("size") String size) {
        ItemRequestDto itemRequestDto = new ItemRequestDto(null, PizzaSize.valueOf(size), quantity);
        cartService.updateItem(itemId, itemRequestDto);

        return "redirect:/cart";
    }

    /**
     * Aktualisiert ein Item im Warenkorb des anonymen(Session) Benutzers.
     *
     * @param itemIdx  ID des zu aktualisierenden Items
     * @param quantity Neue Menge
     * @param size     Neue Größe
     * @return Weiterleitung zur Warenkorb-Seite
     */
    @PostMapping("/update/session/{itemIdx}")
    public String updateSessionItem(@PathVariable Long itemIdx, @RequestParam int quantity, @RequestParam String size) {
        ItemRequestDto itemRequestDto = new ItemRequestDto(null, PizzaSize.valueOf(size), quantity);
        cartService.updateSessionItem(itemIdx, itemRequestDto);

        return "redirect:/cart";
    }
}
