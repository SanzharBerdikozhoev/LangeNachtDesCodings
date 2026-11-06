package whz.pti.eva.user.boundary;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import whz.pti.eva.user.domain.UserCreateForm;
import whz.pti.eva.user.service.user.UserService;
import whz.pti.eva.user.service.validator.UserCreateFormValidator;

import static org.springframework.security.web.context.HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY;

/**
 * Steuert die Registrierung neuer Benutzer in der Pizza-Anwendung.
 * Bietet Endpunkte für das Anzeigen und Verarbeiten des Registrierungsformulars mit Validierung.
 */
@Controller
public class UserController {
    private final UserService userService;
    private final UserCreateFormValidator userCreateFormValidator;
    private final UserDetailsService userDetailsService;

    /**
     * Konstruktor, der UserService und Validator injiziert.
     *
     * @param userService             Service für Benutzeroperationen
     * @param userCreateFormValidator Validator für Registrierungsformular
     */
    public UserController(UserService userService,
                          UserCreateFormValidator userCreateFormValidator,
                          UserDetailsService userDetailsService) {
        this.userService = userService;
        this.userCreateFormValidator = userCreateFormValidator;
        this.userDetailsService = userDetailsService;
    }

    /**
     * Initialisiert den Binder für das Registrierungsformular mit Validator.
     *
     * @param binder WebDataBinder für Validierung
     */
    @InitBinder("create-form")
    public void initBinder(WebDataBinder binder) {
        binder.addValidators(userCreateFormValidator);
    }

    /**
     * Zeigt die Registrierungsseite mit leerem Formular an.
     *
     * @param model Modell für View-Attribute
     * @return Name der View für Registrierung
     */
    @GetMapping("/register")
    public String getUserManagedPage(Model model) {
        model.addAttribute("create-form", new UserCreateForm());
        return "auth/register";
    }

    /**
     * Verarbeitet das Registrierungsformular, validiert und erstellt Benutzer oder zeigt Fehler an.
     *
     * @param form          Registrierungsformular
     * @param bindingResult Ergebnis der Validierung
     * @param model         Modell für View-Attribute
     * @return Redirect zur Startseite bei Erfolg oder View für Registrierung bei Fehler
     */
    @PostMapping("/register")
    public String handleUserCreateForm(@Valid @ModelAttribute("create-form") UserCreateForm form,
                                       BindingResult bindingResult,
                                       Model model,
                                       HttpServletRequest request) {
        if (bindingResult.hasGlobalErrors()) {
            model.addAttribute("error", bindingResult.getGlobalError().getDefaultMessage());
            return "auth/register";
        }
        userService.create(form);


        try {
            UserDetails userDetails = userDetailsService.loadUserByUsername(form.getLoginName());

            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    userDetails,
                    userDetails.getPassword(),
                    userDetails.getAuthorities()
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            request.getSession(true).setAttribute(SPRING_SECURITY_CONTEXT_KEY, SecurityContextHolder.getContext());
        } catch (Exception e) {
            return "redirect:/login?error=autologin_failed";
        }
        return "redirect:/";
    }
}
