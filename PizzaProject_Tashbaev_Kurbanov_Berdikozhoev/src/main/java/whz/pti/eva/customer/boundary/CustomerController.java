package whz.pti.eva.customer.boundary;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import whz.pti.eva.customer.domain.Customer;
import whz.pti.eva.customer.service.CustomerService;
import whz.pti.eva.user.domain.CurrentUser;

import java.util.List;

/**
 * Controller für Kundenoperationen.
 * Verwaltet das Anzeigen, Erstellen, Aktualisieren und Blockieren von Kunden.
 */
@Controller
@RequestMapping("/customers")
public class CustomerController {

    private final CustomerService customerService;

    /**
     * Konstruktor für den CustomerController injiziert den CustomerService.
     *
     * @param customerService Service für Kundenoperationen
     */
    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }


    /**
     * Listet alle Kunden auf.
     *
     * @param model Model für die View
     * @return Name der Thymeleaf-Template-Datei für die Kundenliste
     */
    @GetMapping("")
    public String listCustomers(Model model) {
        List<Customer> customers = customerService.getAll();
        model.addAttribute("customers", customers);
        model.addAttribute("newCustomer", new Customer());
        return "customer/customers";
    }

    /**
     * Erstellt einen neuen Kunden.
     *
     * @param customer Kundenobjekt mit Vorname, Nachname und LoginName
     * @return Weiterleitung zur Kundenliste
     * @throws IllegalArgumentException wenn der Login-Name null ist
     */
    @PostMapping("/create")
    public String create(@ModelAttribute("customer") Customer customer) {
        customerService.create(customer.getFirstName(), customer.getLastName());
        return "redirect:/customers";
    }

    /**
     * Aktualisiert einen vorhandenen Kunden.
     *
     * @param id       ID des Kunden
     * @param customer Kundenobjekt mit neuen Werten
     * @return Weiterleitung zur Kundenliste
     * @throws whz.pti.eva.common.exceptions.CustomerNotFoundException wenn Kunde nicht gefunden
     */
    @PostMapping("/{id}/update")
    public String updateCustomer(@PathVariable("id") Long id, @ModelAttribute("customer") Customer customer) {
        customerService.update(id, customer.getFirstName(), customer.getLastName());

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            CurrentUser currentUser = (CurrentUser) auth.getPrincipal();
            String role = currentUser.getUser().getRole().name();

            if ("ADMIN".equals(role)) {
                return "redirect:/customers";
            } else if ("USER".equals(role)) {
                return "redirect:/profile";
            }
        }

        return "redirect:/";
    }

    /**
     * Blockiert einen Kunden (setzt aktiv=false).
     *
     * @param id ID des Kunden
     * @return Weiterleitung zur Kundenliste
     * @throws whz.pti.eva.common.exceptions.CustomerNotFoundException wenn Kunde nicht gefunden
     */
    @PostMapping("/{id}/block")
    public String blockCustomer(@PathVariable("id") Long id) {
        customerService.block(id);
        return "redirect:/customers";
    }
}
