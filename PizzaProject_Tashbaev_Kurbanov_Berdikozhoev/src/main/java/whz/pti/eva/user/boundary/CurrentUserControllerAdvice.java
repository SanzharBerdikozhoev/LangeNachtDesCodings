package whz.pti.eva.user.boundary;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import whz.pti.eva.user.domain.CurrentUser;

/**
 * ControllerAdvice-Klasse, die den aktuellen Benutzer als Modellattribut für alle Views bereitstellt.
 * Ermöglicht den Zugriff auf CurrentUser-Objekt in Thymeleaf-Templates über ${currentUser}.
 */
@ControllerAdvice
public class CurrentUserControllerAdvice {

    /**
     * Extrahiert den aktuellen Benutzer aus der Authentifizierung und fügt ihn als "currentUser" zum Modell hinzu.
     * @param authentication Aktuelle Authentifizierung
     * @return CurrentUser-Objekt oder null, falls nicht authentifiziert
     */
    @ModelAttribute("currentUser")
    public CurrentUser getCurrentUser(Authentication authentication) {
        return (authentication == null) ? null : (CurrentUser) authentication.getPrincipal();
    }
}
