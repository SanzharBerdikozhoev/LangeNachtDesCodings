package whz.pti.eva.payment.boundary;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import whz.pti.eva.payment.service.PaymentService;
import whz.pti.eva.user.service.currentuser.CurrentUserUtils;

@ControllerAdvice
public class BalanceControllerAdvice {

    private final PaymentService paymentService;

    public BalanceControllerAdvice(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @ModelAttribute
    public void addUserData(Model model) {

        CurrentUserUtils.getUser().ifPresent(user -> {
            model.addAttribute("currentUser", user);
            try {
                double balance = paymentService.getBalance(user.getLoginName());
                model.addAttribute("balance", balance);
            } catch (Exception e) {
                model.addAttribute("balance", 0.0);
            }
        });
    }
}
