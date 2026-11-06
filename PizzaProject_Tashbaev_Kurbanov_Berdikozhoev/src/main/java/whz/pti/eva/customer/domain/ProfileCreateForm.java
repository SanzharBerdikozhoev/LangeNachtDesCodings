package whz.pti.eva.customer.domain;

import jakarta.validation.constraints.NotEmpty;

/**
 * Form-Objekt für die Erstellung neuer Porfil für aktuelle Benutzer.
 * Enthält Felder für Vorname, Nachname.
 */
public class ProfileCreateForm {
    @NotEmpty
    private String firstName = "";

    @NotEmpty
    private String lastName = "";

    /**
     * Gibt den Vorname zurück.
     *
     * @return Vorname
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * Setzt den Vorname.
     *
     * @param firstName Neuer Vorname
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * Gibt den Nachname zurück.
     *
     * @return Nachname
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * Setzt den Nachname.
     *
     * @param lastName Neuer Nachname
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * Gibt eine String-Repräsentation des Formulars zurück.
     *
     * @return String mit Formular-Details
     */
    @Override
    public String toString() {
        return "ProfileCreateForm {" +
                "firstName=" + firstName +
                ", lastName=" + lastName +
                '}';
    }
}
