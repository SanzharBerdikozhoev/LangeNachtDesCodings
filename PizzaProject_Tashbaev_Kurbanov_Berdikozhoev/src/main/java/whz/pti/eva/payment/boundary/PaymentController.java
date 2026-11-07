package whz.pti.eva.payment.boundary;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
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

    @GetMapping("/account")
    public String showAccount(Model model, RedirectAttributes redirectAttributes) {
        Optional<User> currentUser = CurrentUserUtils.getUser();
        if (currentUser.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Bitte loggen Sie sich ein, um Ihr Konto zu sehen.");
            return "redirect:/login?redirect=/payment/account";
        }

        try {
            model.addAttribute("balance", paymentService.getBalance(currentUser.get().getLoginName()));
        } catch (RuntimeException e) {
            model.addAttribute("error", "Kontostand konnte momentan nicht geladen werden.");
        }
        return "payment/account";
    }

    @PostMapping("/deposit")
    public String doDeposit(@RequestParam double amount, RedirectAttributes redirectAttributes) {
        Optional<User> currentUser = CurrentUserUtils.getUser();
        if (currentUser.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Bitte loggen Sie sich ein, um das Geld einzuzahlen.");
            return "redirect:/login?redirect=/ordered";
        }

        if (!Double.isFinite(amount) || amount <= 0) {
            redirectAttributes.addFlashAttribute("error", "Der Einzahlungsbetrag muss größer als 0 sein.");
            return "redirect:/payment/account";
        }

        try {
            paymentService.deposit(currentUser.get().getLoginName(), amount);
            redirectAttributes.addFlashAttribute("success", "Einzahlung erfolgreich!");
        } catch (PaymentFailedException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/payment/account";
    }
}
