package whz.pti.eva.customer.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Formular-Objekt zur Aufnahme und Validierung der Daten einer Lieferadresse.
 *
 * Wird vom Controller zur Entgegennahme von Adressdaten aus der UI verwendet.
 */
public class DeliveryAddressForm {

    @NotBlank
    private String street;

    @NotBlank
    private String houseNumber;

    @NotBlank
    private String town;

    /**
     * Die Postleitzahl. Muss ausgefüllt sein und dem Muster für 5- bis 6-stellige Ziffern entsprechen.
     *
     * Muster: {@code \\d{5,6}} (5 bis 6 Ziffern)
     */
    @NotBlank
    @Pattern(regexp = "\\d{5,6}")
    private String postalCode;

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getHouseNumber() {
        return houseNumber;
    }

    public void setHouseNumber(String houseNumber) {
        this.houseNumber = houseNumber;
    }

    public String getTown() {
        return town;
    }

    public void setTown(String town) {
        this.town = town;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }
}