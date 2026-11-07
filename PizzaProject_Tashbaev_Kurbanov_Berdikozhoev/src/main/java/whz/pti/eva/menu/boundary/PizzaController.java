package whz.pti.eva.menu.boundary;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import whz.pti.eva.cart.service.CartService;
import whz.pti.eva.menu.domain.Pizza;
import whz.pti.eva.menu.service.PizzaService;
import whz.pti.eva.user.domain.User;
import whz.pti.eva.user.service.currentuser.CurrentUserUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Optional;

/**
 * Steuert die Web-Anfragen für das Pizza-Entity und die Verwaltung von Pizzen in der Pizza-Anwendung.
 * Bietet Endpunkte für das Anzeigen, Hinzufügen, Bearbeiten und Löschen von Pizzen sowie Integration mit Warenkorb.
 */
@Controller
public class PizzaController {

    private final PizzaService pizzaService;
    private final CartService cartService;

    /**
     * Konstruktor, der die notwendigen Services injiziert.
     * @param pizzaService Service für Pizza-Operationen
     * @param cartService Service für Warenkorb-Operationen
     */
    public PizzaController(PizzaService pizzaService, CartService cartService) {
        this.pizzaService = pizzaService;
        this.cartService = cartService;
    }

    /**
     * Zeigt alle Pizzen und den aktuellen Warenkorb für den Benutzer an.
     * Unterstützt sowohl authentifizierte als auch anonyme Benutzer.
     * @param model Modell für die View-Attribute
     * @return Name der View für das Pizza-Menü
     */
    @GetMapping("/")
    public String getAllPizza(@RequestParam(defaultValue = "") String q,
                              @RequestParam(defaultValue = "name") String sort,
                              @RequestParam(defaultValue = "asc") String direction,
                              Model model) {
        Optional<User> currentUser = CurrentUserUtils.getUser();

        if(currentUser.isEmpty()) {
            model.addAttribute("sessionCart", cartService.getSessionItems());
        } else {
            String loginName = currentUser.get().getLoginName();
            cartService.mergeSessionCartIntoDb(loginName);
            model.addAttribute("cart", cartService.findByUserId(loginName));
        }

        var pizzas = new ArrayList<>(pizzaService.searchByName(q));
        Comparator<Pizza> comparator = "price".equals(sort)
                ? Comparator.comparing(Pizza::getPriceMedium)
                : Comparator.comparing(Pizza::getName, String.CASE_INSENSITIVE_ORDER);
        if ("desc".equals(direction)) {
            comparator = comparator.reversed();
        }
        pizzas.sort(comparator);
        model.addAttribute("pizzas", pizzas);
        model.addAttribute("searchQuery", q);
        model.addAttribute("sort", "price".equals(sort) ? "price" : "name");
        model.addAttribute("direction", "desc".equals(direction) ? "desc" : "asc");

        return "pizza/pizza-menu";
    }

    /**
     * Zeigt die Verwaltungsseite für Pizzen mit Liste aller Pizzen und Formular für neue Pizza.
     * @param model Modell für die View-Attribute
     * @return Name der View für Pizza-Verwaltung
     */
    @GetMapping("/pizzas")
    public String showPizzaManagement(Model model) {
        model.addAttribute("pizzas", pizzaService.listAll());
        model.addAttribute("newPizza", new Pizza());
        return "pizza/pizza-management";
    }

    /**
     * Aktualisiert eine bestehende Pizza anhand der ID.
     * @param id ID der zu aktualisierenden Pizza
     * @param pizza Aktualisierte Pizza-Daten
     * @return Redirect zur Pizza-Verwaltungsseite
     */
    @PostMapping("/pizzas/update/{id}")
    public String updatePizza(@PathVariable("id") Long id, @ModelAttribute("pizza") Pizza pizza) {
        pizzaService.updateById(id, pizza);
        return "redirect:/pizzas";
    }

    /**
     * Zeigt die Bearbeitungsseite für eine spezifische Pizza.
     * @param id ID der zu bearbeitenden Pizza
     * @param model Modell für die View-Attribute
     * @return Name der View für Pizza-Bearbeitung
     */
    @GetMapping("/pizzas/edit/{id}")
    public String editPizzaPage(@PathVariable("id") Long id, Model model) {
        Pizza pizza = pizzaService.getById(id).orElseThrow(() -> new IllegalArgumentException("Invalid pizza ID"));
        model.addAttribute("pizza", pizza);
        return "pizza/pizza-edit";
    }

    /**
     * Löscht eine Pizza anhand der ID.
     * @param id ID der zu löschenden Pizza
     * @return Redirect zur Pizza-Verwaltungsseite
     */
    @GetMapping("/pizzas/delete/{id}")
    public String deletePizza(@PathVariable("id") Long id) {
        pizzaService.deleteById(id);
        return "redirect:/pizzas";
    }

    /**
     * Fügt eine neue Pizza zum Katalog hinzu.
     * @param pizza Neue Pizza-Daten
     * @return Redirect zur Pizza-Verwaltungsseite
     */
    @PostMapping("/pizzas/add")
    public String addPizza(@ModelAttribute("newPizza") Pizza pizza) {
        pizzaService.addPizza(pizza);
        return "redirect:/pizzas";
    }
}
