package whz.pti.eva.user.service.user;

import whz.pti.eva.user.domain.User;
import whz.pti.eva.user.domain.UserCreateForm;

import java.util.Optional;

/**
 * Service-Schnittstelle für Benutzeroperationen in der Pizza-Anwendung.
 * Ermöglicht das Suchen, Überprüfen und Erstellen von Benutzern.
 */
public interface UserService {

    /**
     * Findet einen Benutzer anhand seines Login-Namens.
     * @param username Login-Name des Benutzers
     * @return Optional mit dem User, falls gefunden
     */
    Optional<User> findByLoginName(String username);

    /**
     * Überprüft, ob ein Benutzer mit dem gegebenen Login-Namen existiert.
     * @param loginName Der zu prüfende Login-Name
     * @return true, falls der Benutzer existiert
     */
    boolean existsByLoginName(String loginName);

    /**
     * Erstellt einen neuen Benutzer aus dem Registrierungsformular.
     * @param form Formular mit Benutzerdaten
     * @return Erstelleter User
     */
    User create(UserCreateForm form);
}

