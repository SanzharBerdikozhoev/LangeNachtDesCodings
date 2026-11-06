package whz.pti.eva.customer.boundary;

import jakarta.validation.Valid;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import whz.pti.eva.customer.domain.*;
import whz.pti.eva.customer.service.CustomerService;
import whz.pti.eva.customer.service.DeliveryAddressService;
import whz.pti.eva.user.domain.User;
import whz.pti.eva.user.service.currentuser.CurrentUserUtils;

import java.util.Optional;

/**
 * Controller zur Verwaltung des Kundenprofils (Customer) und der zugehörigen Ansichten.
 * <p>
 * Stellt Methoden zum Anzeigen des Profils und zum Erstellen eines neuen Kundenprofils bereit.
 */
@Controller
@RequestMapping("profile")
public class ProfileController {

    private final CustomerService customerService;
    private final DeliveryAddressService deliveryAddressService;


    /**
     * Konstruktor, injiziert den CustomerService.
     *
     * @param customerService Service zur Verwaltung der Kundendaten.
     * @param deliveryAddressService zur Verwaltung der Adressen.
     */
    public ProfileController(CustomerService customerService, DeliveryAddressService deliveryAddressService) {
        this.customerService = customerService;
        this.deliveryAddressService = deliveryAddressService;
    }

    /**
     * Zeigt die Profilseite des aktuell eingeloggten Benutzers an.
     * <p>
     * Lädt das Kundenobjekt, falls vorhanden, und stellt das Adressformular bereit.
     *
     * @param model Das Spring UI Model.
     * @return Der Name der View-Seite ("customer/profile").
     * @throws UsernameNotFoundException wenn kein eingeloggter Benutzer gefunden wird.
     */
    @GetMapping("")
    public String showProfile(Model model) {
        Optional<User> currentUser = CurrentUserUtils.getUser();
        if (currentUser.isEmpty()) {
            throw new UsernameNotFoundException("User wurde nicht gefunden");
        }

        boolean hasCustomer = customerService.hasCustomer(currentUser.get());
        model.addAttribute("hasCustomer", hasCustomer);

        if (hasCustomer) {
            Customer customer = customerService.findByUser(currentUser.get());
            model.addAttribute("customer", customer);
        }

        if (!model.containsAttribute("form")) {
            model.addAttribute("form", new DeliveryAddressForm());
        }

        return "customer/profile";
    }

    /**
     * Zeigt das Formular zur Erstellung eines neuen Kundenprofils an.
     *
     * @param model Das Spring UI Model.
     * @return Der Name der View-Seite ("customer/create-profile").
     */
    @GetMapping("/create")
    public String getProfileCreateFormPage(Model model) {
        model.addAttribute("profile-create-form", new ProfileCreateForm());
        return "customer/create-profile";
    }

    /**
     * Verarbeitet die POST-Anfrage zur Erstellung eines Kundenprofils.
     * <p>
     * Ruft den CustomerService zur Speicherung der Daten auf und leitet zur Profilseite weiter.
     *
     * @param form  Das validierte Formularobjekt zur Profilerstellung.
     * @param model Das Spring UI Model.
     * @return Weiterleitung zur Profilseite.
     */
    @PostMapping("/create")
    public String handleProfileCreateForm(@Valid @ModelAttribute("profile-create-form") ProfileCreateForm form, Model model) {
        customerService.create(form.getFirstName(), form.getLastName());
        return "redirect:/profile";
    }

    /**
     * Verarbeitet die POST-Anfrage zur Erstellung eines Kundenprofils inkl. Adresse.
     * <p>
     * Validiert das übergebene Formular. Bei Validierungsfehlern werden die Fehler und das
     * Formular als Flash-Attribute gespeichert und der Benutzer zurück zur Profilseite geleitet.
     * Ansonsten werden Customer und zugehörige DeliveryAddress erstellt und gespeichert.
     *
     * @param form               Das validierte Formularobjekt zur Profilerstellung inkl. Adresse.
     * @param result             BindingResult für Validierungsfehler.
     * @param redirectAttributes Spring RedirectAttributes zur Übergabe von Flash-Attributen.
     * @return Weiterleitung zur Profilseite (redirect:/profile).
     */
    @PostMapping("/create-full")
    public String createProfileAndAddress(
            @Valid @ModelAttribute("form") ProfileAndAddressForm form,
            BindingResult result,
            RedirectAttributes redirectAttributes
    ) {
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.form", result);
            redirectAttributes.addFlashAttribute("form", form);
            return "redirect:/profile";
        }

        Customer customer = customerService.create(
                form.getFirstName(),
                form.getLastName()
        );

        DeliveryAddress address = new DeliveryAddress(
                form.getStreet(),
                form.getHouseNumber(),
                form.getTown(),
                form.getPostalCode()
        );

        deliveryAddressService.save(address, customer.getId());

        redirectAttributes.addFlashAttribute("success", "Profil und Adresse wurden erstellt.");

        return "redirect:/cart";
    }
}
