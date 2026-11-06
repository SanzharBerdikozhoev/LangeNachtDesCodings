package whz.pti.eva.ordered.boundary;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import whz.pti.eva.common.exceptions.PaymentFailedException;
import whz.pti.eva.ordered.domain.Ordered;
import whz.pti.eva.ordered.service.OrderedWithDeliveryAddressDto;
import whz.pti.eva.ordered.service.OrderedService;
import whz.pti.eva.ordered.service.PendingOrderDTO;
import whz.pti.eva.user.domain.User;
import whz.pti.eva.user.service.currentuser.CurrentUserUtils;

import java.util.List;
import java.util.Optional;

/**
 * Controller-Klasse für die Verwaltung von Bestellungen über HTTP-Anfragen.
 * Stellt Endpunkte bereit, um Bestellungen eines Benutzers anzuzeigen, hinzuzufügen oder zu löschen.
 */
@Controller
@RequestMapping("/ordered")
public class OrderedController {

    private final OrderedService orderedService;

    /**
     * Konstruktor für den {@link OrderedController}.
     *
     * @param orderedService Service zur Verwaltung der Bestelllogik
     */
    public OrderedController(OrderedService orderedService) {
        this.orderedService = orderedService;
    }

    /**
     * Zeigt alle Bestellungen des aktuell authentifizierten Benutzers an.
     * <p>
     * Falls kein Benutzer eingeloggt ist, erfolgt ein Redirect zur Login-Seite.
     *
     * @param model              Spring-Model zur Übergabe der Bestellungen an die View
     * @param redirectAttributes Attribute für Fehlermeldungen nach Redirect
     * @return Thymeleaf-Template für die Bestellübersicht oder Redirect zur Login-Seite
     */
    @GetMapping("")
    public String getAllOrdered(Model model, RedirectAttributes redirectAttributes) {
        Optional<User> currentUser = CurrentUserUtils.getUser();
        if (currentUser.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Bitte loggen Sie sich ein, um eine Bestellung zu sehen.");
            return "redirect:/login?redirect=/ordered/complete";
        }

        String loginName = currentUser.get().getLoginName();
        List<OrderedWithDeliveryAddressDto> orderedWithAddress = orderedService.getAllOrderedWithAddress(loginName);

        model.addAttribute("ordered", orderedWithAddress);
        return "ordered/ordered";
    }

    /**
     * Erstellt eine neue Bestellung für den authentifizierten Benutzer.
     * <p>
     * Wenn der Benutzer nicht eingeloggt ist, werden die ausgewählten Artikel in einem Cookie
     * gespeichert und der Benutzer zur Login-Seite weitergeleitet.
     *
     * @param itemIds            IDs der ausgewählten Cart-Items, die bestellt werden sollen
     * @param redirectAttributes Flash-Attribute für Erfolgsmeldungen oder Fehlernachrichten
     * @param deliveryAddressId  ID der ausgewählten Lieferadresse, die der Bestellung zugeordnet werden soll
     * @return Redirect zur Bestellübersicht oder zur Login-Seite
     */
    @PostMapping("")
    public String addOrdered(@RequestParam("selectedItems") List<Long> itemIds,
                             @RequestParam("deliveryAddressId") Long deliveryAddressId,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        Optional<User> currentUser = CurrentUserUtils.getUser();
        if (currentUser.isEmpty()) {
            PendingOrderDTO pendingOrderDTO = new PendingOrderDTO(itemIds, deliveryAddressId);

            session.setAttribute("PENDING_ORDER", pendingOrderDTO);

            redirectAttributes.addFlashAttribute("error", "Bitte loggen Sie sich ein, um eine Bestellung abzuschließen.");
            return "redirect:/login?redirect=/ordered";
        }

        String loginName = currentUser.get().getLoginName();
        try {
            orderedService.addOrdered(loginName, itemIds, deliveryAddressId);

            redirectAttributes.addFlashAttribute("success",
                    "Bestellung erfolgreich erstellt!");

            return "redirect:/ordered";

        } catch (PaymentFailedException e) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "Sie haben nicht genügend Guthaben auf Ihrem Konto. Bitte laden Sie Ihr Konto auf."
            );


            return "redirect:/cart";
        }

    }

    /**
     * Löscht eine gegebene Bestellung.
     *
     * @param ordered Bestellung, die gelöscht werden soll (ModelAttribute)
     * @return Redirect zur Startseite
     */
    @DeleteMapping("")
    public String deleteOrdered(@ModelAttribute("ordered") Ordered ordered) {
        orderedService.deleteOrdered(ordered);
        return "redirect:/";
    }
}
