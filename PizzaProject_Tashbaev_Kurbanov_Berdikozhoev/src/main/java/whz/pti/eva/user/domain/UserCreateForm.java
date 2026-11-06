package whz.pti.eva.user.domain;

import jakarta.validation.constraints.NotEmpty;

/**
 * Form-Objekt für die Erstellung neuer Benutzer in der Registrierung der Pizza-Anwendung.
 * Enthält Felder für Login, Passwort (mit Wiederholung) und Rolle mit Validierungen.
 */
public class UserCreateForm {

    @NotEmpty
    private String firstName = "";

    @NotEmpty
    private String lastName = "";

    @NotEmpty
    private String loginName = "";

    @NotEmpty
    private String password = "";

    @NotEmpty
    private String passwordRepeated = "";

    private Role role = Role.USER;

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * Gibt den Login-Namen zurück.
     * @return Login-Name
     */
    public String getLoginName() {
        return loginName;
    }

    /**
     * Setzt den Login-Namen.
     * @param loginName Neuer Login-Name
     */
    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }

    /**
     * Gibt das Passwort zurück.
     * @return Passwort
     */
    public String getPassword() {
        return password;
    }

    /**
     * Setzt das Passwort.
     * @param password Neues Passwort
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Gibt die Passwort-Wiederholung zurück.
     * @return Wiederholtes Passwort
     */
    public String getPasswordRepeated() {
        return passwordRepeated;
    }

    /**
     * Setzt die Passwort-Wiederholung.
     * @param passwordRepeated Neues wiederholtes Passwort
     */
    public void setPasswordRepeated(String passwordRepeated) {
        this.passwordRepeated = passwordRepeated;
    }

    /**
     * Gibt die Rolle zurück.
     * @return Benutzer-Rolle
     */
    public Role getRole() {
        return role;
    }

    /**
     * Setzt die Rolle.
     * @param role Neue Rolle
     */
    public void setRole(Role role) {
        this.role = role;
    }

    /**
     * Gibt eine String-Repräsentation des Formulars zurück (Passwörter maskiert).
     * @return String mit Formular-Details
     */
    @Override
    public String toString() {
        return "UserCreateForm {" +
                "loginName=" + loginName +
                ", password=***" + '\'' +
                ", passwordRepeated=***" + '\'' +
                ", role=" + role +
                ", firstName=" + firstName +
                ", lastName=" + lastName +
                '}';
    }
}
