package whz.pti.eva.user.service.currentuser;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import whz.pti.eva.user.domain.CurrentUser;
import whz.pti.eva.user.domain.User;

import java.util.Optional;

/**
 * Utility-Klasse zum einfachen Abrufen des aktuell eingeloggten Benutzers aus dem Spring Security Context.
 * <p>
 * Stellt statische Methoden bereit, um das {@code CurrentUser}- oder das zugrundeliegende {@code User}-Objekt zu erhalten.
 * Die Klasse ist final und der Konstruktor ist privat, um die Instanziierung zu verhindern.
 */
public final class CurrentUserUtils {
    private CurrentUserUtils() {
    }

    /**
     * Liefert das {@code CurrentUser}-Objekt, das den Principal des eingeloggten Benutzers darstellt.
     *
     * @return Ein Optional, das das {@code CurrentUser}-Objekt enthält, falls der Benutzer eingeloggt ist und der Principal vom Typ {@code CurrentUser} ist.
     */
    public static Optional<CurrentUser> getCurrentUser() {
        Authentication authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();

        if (authentication == null) {
            return Optional.empty();
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof CurrentUser currentUser) {
            return Optional.of(currentUser);
        }

        return Optional.empty();
    }

    /**
     * Liefert das zugrundeliegende persistente {@code User}-Objekt des aktuell eingeloggten Benutzers.
     *
     * @return Ein Optional, das das {@code User}-Objekt enthält, falls der Benutzer eingeloggt ist.
     */
    public static Optional<User> getUser() {
        return getCurrentUser().map(CurrentUser::getUser);
    }
}
