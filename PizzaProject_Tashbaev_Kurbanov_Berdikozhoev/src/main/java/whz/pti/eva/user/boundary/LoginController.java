package whz.pti.eva.user.boundary;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Steuert den Zugriff auf die Login-Seite in der Pizza-Anwendung.
 * Behandelt GET-Anfragen für den Login und zeigt Fehlermeldungen bei ungültigen Anmeldedaten an.
 */
@Controller
public class LoginController {

    /**
     * Zeigt die Login-Seite an und fügt bei Fehler eine Fehlermeldung zum Modell hinzu.
     * @param error Optionaler Fehlerparameter aus der Anfrage
     * @param model Modell für View-Attribute
     * @return Name der View für die Login-Seite
     */
    @GetMapping("/login")
    public String getLoginPage(@RequestParam(value = "error", required = false) String error,
                               @RequestParam(value = "redirect", required = false) String redirect,
                               HttpSession session,
                               Model model) {
        if (error != null) {
            model.addAttribute("errorMessage", "Falscher Loginname oder Passwort");
        }

        if (redirect != null && !redirect.isBlank()) {
            session.setAttribute("LOGIN_REDIRECT", redirect);
        }

        return "auth/login";
    }
}