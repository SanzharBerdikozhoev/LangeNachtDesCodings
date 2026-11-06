package whz.pti.eva.customer.domain;

import jakarta.validation.constraints.NotBlank;

/**
 * Form-Klasse zur Erfassung von Profil- und Adressdaten eines Kunden.
 * Wird verwendet, wenn der Kunde noch kein Profil und/oder keine Adresse
 * hinterlegt hat.
 */
public class ProfileAndAddressForm {

    /** Vorname des Kunden. Darf nicht leer sein. */
    @NotBlank
    private String firstName;

    /** Nachname des Kunden. Darf nicht leer sein. */
    @NotBlank
    private String lastName;

    /** Straße der Lieferadresse. Darf nicht leer sein. */
    @NotBlank
    private String street;

    /** Hausnummer der Lieferadresse. Darf nicht leer sein. */
    @NotBlank
    private String houseNumber;

    /** Stadt der Lieferadresse. Darf nicht leer sein. */
    @NotBlank
    private String town;

    /** Postleitzahl der Lieferadresse. Darf nicht leer sein. */
    @NotBlank
    private String postalCode;

    /**
     * Gibt den Vornamen des Kunden zurück.
     *
     * @return der Vorname
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * Setzt den Vornamen des Kunden.
     *
     * @param firstName der Vorname
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * Gibt den Nachnamen des Kunden zurück.
     *
     * @return der Nachname
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * Setzt den Nachnamen des Kunden.
     *
     * @param lastName der Nachname
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * Gibt die Straße der Lieferadresse zurück.
     *
     * @return die Straße
     */
    public String getStreet() {
        return street;
    }

    /**
     * Setzt die Straße der Lieferadresse.
     *
     * @param street die Straße
     */
    public void setStreet(String street) {
        this.street = street;
    }

    /**
     * Gibt die Hausnummer der Lieferadresse zurück.
     *
     * @return die Hausnummer
     */
    public String getHouseNumber() {
        return houseNumber;
    }

    /**
     * Setzt die Hausnummer der Lieferadresse.
     *
     * @param houseNumber die Hausnummer
     */
    public void setHouseNumber(String houseNumber) {
        this.houseNumber = houseNumber;
    }

    /**
     * Gibt die Stadt der Lieferadresse zurück.
     *
     * @return die Stadt
     */
    public String getTown() {
        return town;
    }

    /**
     * Setzt die Stadt der Lieferadresse.
     *
     * @param town die Stadt
     */
    public void setTown(String town) {
        this.town = town;
    }

    /**
     * Gibt die Postleitzahl der Lieferadresse zurück.
     *
     * @return die Postleitzahl
     */
    public String getPostalCode() {
        return postalCode;
    }

    /**
     * Setzt die Postleitzahl der Lieferadresse.
     *
     * @param postalCode die Postleitzahl
     */
    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }
}
