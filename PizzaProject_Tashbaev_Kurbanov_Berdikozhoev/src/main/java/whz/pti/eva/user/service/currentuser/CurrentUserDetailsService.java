package whz.pti.eva.user.service.currentuser;

import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import whz.pti.eva.user.domain.CurrentUser;
import whz.pti.eva.user.domain.User;
import whz.pti.eva.user.service.user.UserService;

/**
 * Spring Security UserDetailsService-Implementierung für die Pizza-Anwendung.
 * Lädt Benutzerdaten basierend auf Login-Name und wandelt sie in CurrentUser um.
 */
@Service
public class CurrentUserDetailsService implements UserDetailsService {

    private UserService userService;

    /**
     * Konstruktor, der den UserService injiziert.
     *
     * @param userService Service für Benutzeroperationen
     */
    public CurrentUserDetailsService(UserService userService) {
        this.userService = userService;
    }

    /**
     * Lädt den Benutzer anhand des Login-Namens und gibt CurrentUser zurück.
     * Wirft UsernameNotFoundException bei nicht gefundenem Benutzer.
     *
     * @param loginName Login-Name des Benutzers
     * @return CurrentUser-Objekt
     * @throws UsernameNotFoundException falls Benutzer nicht existiert
     */
    @Override
    public CurrentUser loadUserByUsername(String loginName) throws UsernameNotFoundException {
        User user = userService.findByLoginName(loginName).orElseThrow(() ->
                        new UsernameNotFoundException("User with email= " + loginName + " cannot be not found"));
        return new CurrentUser(user);
    }
}
