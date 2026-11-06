package whz.pti.eva.payment.boundary;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import whz.pti.eva.common.exceptions.PaymentFailedException;
import whz.pti.eva.payment.service.PaymentService;
import whz.pti.eva.user.domain.User;
import whz.pti.eva.user.service.currentuser.CurrentUserUtils;

import java.util.Optional;

@Controller
@RequestMapping("/payment")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/deposit")
    public String doDeposit(@RequestParam double amount, RedirectAttributes redirectAttributes) {
        Optional<User> currentUser = CurrentUserUtils.getUser();
        if (currentUser.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Bitte loggen Sie sich ein, um das Geld einzuzahlen.");
            return "redirect:/login?redirect=/ordered";
        }

        try {
            paymentService.deposit(currentUser.get().getLoginName(), amount);
            redirectAttributes.addFlashAttribute("success", "Einzahlung erfolgreich!");
        } catch (PaymentFailedException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/";
    }
}
