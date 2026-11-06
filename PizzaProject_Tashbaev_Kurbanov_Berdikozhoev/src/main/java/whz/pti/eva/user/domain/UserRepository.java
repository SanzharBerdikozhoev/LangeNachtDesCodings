package whz.pti.eva.user.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * JPA-Repository-Schnittstelle für die Persistenz von User-Entitäten in der Pizza-Anwendung.
 * Erweitert um benutzerdefinierte Abfragen für Login-Name.
 */
public interface UserRepository extends JpaRepository<User, Long> {
    /**
     * Findet einen Benutzer anhand seines Login-Namens.
     * @param loginName Der Login-Name des Benutzers
     * @return Optional mit dem User, falls gefunden
     */
    Optional<User> findByLoginName(String loginName);

    /**
     * Überprüft, ob ein Benutzer mit dem gegebenen Login-Namen existiert.
     * @param loginName Der zu prüfende Login-Name
     * @return true, falls der Benutzer existiert
     */
    boolean existsByLoginName(String loginName);
}
