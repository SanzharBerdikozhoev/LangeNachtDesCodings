package whz.pti.eva.customer.boundary;

import jakarta.validation.Valid;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import whz.pti.eva.customer.domain.Customer;
import whz.pti.eva.customer.domain.DeliveryAddress;
import whz.pti.eva.customer.domain.DeliveryAddressForm;
import whz.pti.eva.customer.service.DeliveryAddressService;
import whz.pti.eva.user.domain.User;
import whz.pti.eva.user.service.currentuser.CurrentUserUtils;

import java.util.Optional;

/**
 * Controller zur Verwaltung der Lieferadressen eines eingeloggten Benutzers.
 * <p>
 * Erlaubt das Hinzufügen und Löschen von Adressen über POST-Anfragen.
 * Die Funktionalität berücksichtigt die Sicherheitsanforderungen,
 * indem nur eingeloggte Benutzer (Rolle 'User') ihre eigenen Adressen bearbeiten dürfen[cite: 138].
 */
@Controller
@RequestMapping("address")
public class DeliveryAddressController {

    private final DeliveryAddressService deliveryAddressService;

    /**
     * Konstruktor, der den Adress-Service injiziert.
     *
     * @param deliveryAddressService Der Service zur Verwaltung der Adressen.
     */
    public DeliveryAddressController(DeliveryAddressService deliveryAddressService) {
        this.deliveryAddressService = deliveryAddressService;
    }

    /**
     * Verarbeitet die POST-Anfrage zum Hinzufügen einer neuen Lieferadresse.
     * <p>
     * Prüft die Validierung des Formulars und stellt sicher, dass der Benutzer eingeloggt ist
     * und ein Kundenprofil besitzt, bevor die Adresse gespeichert wird.
     *
     * @param redirectUrl        Die optionale URL, zu der nach erfolgreicher Speicherung weitergeleitet werden soll.
     * @param form               Das validierte Formularobjekt mit den Adressdaten.
     * @param result             Das Ergebnis der Validierung.
     * @param redirectAttributes Attribute für die Weiterleitung (z.B. Erfolgs- oder Fehlermeldungen).
     * @return Die Weiterleitung zur Profilseite oder zur optionalen redirectUrl.
     * @throws UsernameNotFoundException wenn kein eingeloggter Benutzer gefunden wird.
     * @throws IllegalStateException     wenn der gefundene User kein zugehöriges Customer-Profil hat.
     */
    @PostMapping("/add")
    public String addAddress(@RequestParam(value = "redirectUrl", required = false) String redirectUrl,
                             @Valid @ModelAttribute("form") DeliveryAddressForm form,
                             BindingResult result,
                             RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.form", result);
            redirectAttributes.addFlashAttribute("form", form);
            return "redirect:/profile";
        }

        Optional<User> currentUser = CurrentUserUtils.getUser();

        if (currentUser.isEmpty()) {
            throw new UsernameNotFoundException("User wurde nicht gefunden");
        }

        Customer customer = currentUser.get().getCustomer();

        if (customer == null) {
            throw new IllegalStateException("User hat keine profile");
        }

        DeliveryAddress address = new DeliveryAddress(
                form.getStreet(),
                form.getHouseNumber(),
                form.getTown(),
                form.getPostalCode()
        );

        deliveryAddressService.save(address, customer.getId());

        redirectAttributes.addFlashAttribute("success", "Address wurde erstellt.");

        if (redirectUrl != null && !redirectUrl.isEmpty()) {
            return "redirect:" + redirectUrl;
        }

        return "redirect:/profile";
    }

    /**
     * Verarbeitet die POST-Anfrage zum Löschen einer Lieferadresse anhand ihrer ID.
     * <p>
     * Stellt sicher, dass nur der eingeloggte Benutzer seine eigenen Adressen löschen kann.
     * Im Fehlerfall (z.B. Adresse gehört nicht zum Benutzer oder es ist die letzte Adresse)
     * wird eine Fehlermeldung hinzugefügt.
     *
     * @param id                 Die ID der zu löschenden Adresse.
     * @param redirectAttributes Attribute für die Weiterleitung (z.B. Erfolgs- oder Fehlermeldungen).
     * @return Die Weiterleitung zur Profilseite.
     * @throws UsernameNotFoundException wenn kein eingeloggter Benutzer gefunden wird.
     * @throws IllegalStateException     wenn der gefundene User kein zugehöriges Customer-Profil hat.
     */
    @PostMapping("/delete/{id}")
    public String deleteAddress(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        Optional<User> currentUser = CurrentUserUtils.getUser();
        if (currentUser.isEmpty()) {
            throw new UsernameNotFoundException("User wurde nicht gefunden");
        }

        Customer customer = currentUser.get().getCustomer();
        if (customer == null) {
            throw new IllegalStateException("User hat keine Profile");
        }

        try {
            deliveryAddressService.delete(id, customer.getId());
            redirectAttributes.addFlashAttribute("message", "Die Adresse wurde gelöscht");
            redirectAttributes.addFlashAttribute("messageType", "success");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("message", "Die letzte Adresse kann nicht gelöscht werden");
            redirectAttributes.addFlashAttribute("messageType", "warning");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", "Die Adresse konnte nicht gelöscht werden");
            redirectAttributes.addFlashAttribute("messageType", "warning");
        }

        return "redirect:/profile";
    }

    /**
     * Verarbeitet die POST-Anfrage zur Aktualisierung einer bestehenden Lieferadresse.
     * <p>
     * Stellt sicher, dass nur der eingeloggte Benutzer seine eigenen Adressen bearbeiten kann.
     * Ruft den Service zur Durchführung der Aktualisierung auf.
     *
     * @param id                 Die ID der zu aktualisierenden Adresse.
     * @param address            Das ModelAttribute mit den neuen Adressdetails.
     * @param redirectAttributes Attribute für die Weiterleitung (Erfolgs-/Fehlermeldungen).
     * @return Die Weiterleitung zur Profilseite.
     * @throws UsernameNotFoundException wenn kein eingeloggter Benutzer gefunden wird.
     * @throws IllegalStateException     wenn der Benutzer kein zugehöriges Customer-Profil hat.
     */
    @PostMapping("/update/{id}")
    public String updateAddress(@PathVariable("id") Long id,
                                @ModelAttribute("address") DeliveryAddress address,
                                RedirectAttributes redirectAttributes) {
        Optional<User> currentUser = CurrentUserUtils.getUser();
        if (currentUser.isEmpty()) {
            throw new UsernameNotFoundException("User wurde nicht gefunden");
        }

        Customer customer = currentUser.get().getCustomer();
        if (customer == null) {
            throw new IllegalStateException("User hat keine Profile");
        }

        try {
            deliveryAddressService.update(id, address, customer.getId());
            redirectAttributes.addFlashAttribute("message", "Die Adresse wurde aktualisiert");
            redirectAttributes.addFlashAttribute("messageType", "success");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", "Die Adresse konnte nicht aktualisiert werden");
            redirectAttributes.addFlashAttribute("messageType", "warning");
        }

        return "redirect:/profile";
    }
}
