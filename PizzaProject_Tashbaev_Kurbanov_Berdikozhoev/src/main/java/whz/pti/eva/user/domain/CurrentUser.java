package whz.pti.eva.user.domain;

import org.springframework.security.core.authority.AuthorityUtils;

/**
 * Erweiterung der Spring Security User-Klasse für den aktuellen Benutzer in der Pizza-Anwendung.
 * Kombiniert Benutzerdaten mit Authentifizierung und Rollenzugriff.
 */
public class CurrentUser extends org.springframework.security.core.userdetails.User {
    private User user;

    /**
     * Konstruktor, der ein User-Objekt in die Security-User umwandelt.
     * Setzt Login, Passwort und Rolle als Authorities.
     * @param user Das zugehörige User-Objekt
     */
    public  CurrentUser(User user) {
        super(user.getLoginName(), user.getPasswordHash(), AuthorityUtils.createAuthorityList(user.getRole().toString()));
        this.user = user;
    }
    /**
     * Gibt das zugehörige User-Objekt zurück.
     * @return Das User-Objekt
     */
    public User getUser() {
        return user;
    }

    /**
     * Setzt das zugehörige User-Objekt.
     * @param user Neues User-Objekt
     */
    public void setUser(User user) {
        this.user = user;
    }

    /**
     * Gibt die ID des Benutzers zurück.
     * @return Benutzer-ID
     */
    public Long getId() {
        return user.getId();
    }

    /**
     * Gibt den Login-Namen des Benutzers zurück.
     * @return Login-Name
     */
    public String getLoginName() {return user.getLoginName();}

    /**
     * Gibt die Rolle des Benutzers zurück.
     * @return Benutzer-Rolle
     */
    public Role getRole() {
        return user.getRole();
    }

    /**
     * Gibt eine String-Repräsentation des CurrentUser zurück.
     * @return String mit User-Details
     */
    @Override
    public String toString() {
        return "CurrentUser {" +
                "user=" + user +
                "} " + super.toString();
    }
}
