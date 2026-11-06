package whz.pti.eva.customer.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;
import whz.pti.eva.common.domain.BaseEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * Entität zur Speicherung einer Lieferadresse.
 * <p>
 * Speichert Straße, Hausnummer, Stadt und Postleitzahl.
 * Steht in einer Many-to-Many-Beziehung zur Customer-Entität.
 */
@Entity
public class DeliveryAddress extends BaseEntity<Long> {

    private String street;

    private String houseNumber;

    private String town;

    private String postalCode;

    /**
     * Viele-zu-Viele-Beziehung zur Customer-Entität.
     * Dies ist die inverse Seite der Beziehung, gemappt durch das Feld {@code deliveryAddresses} in der Customer-Klasse.
     */
    @ManyToMany(mappedBy = "deliveryAddresses")
    private List<Customer> customers = new ArrayList<>();

    public DeliveryAddress() {
    }

    public DeliveryAddress(String street, String houseNumber, String town, String postalCode) {
        this.street = street;
        this.houseNumber = houseNumber;
        this.town = town;
        this.postalCode = postalCode;
    }

    public String getStreet() {
        return this.street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getHouseNumber() {
        return this.houseNumber;
    }

    public void setHouseNumber(String houseNumber) {
        this.houseNumber = houseNumber;
    }

    public String getTown() {
        return this.town;
    }

    public void setTown(String town) {
        this.town = town;
    }

    public String getPostalCode() {
        return this.postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public List<Customer> getCustomers() {
        return customers;
    }


    @Override
    public String toString() {
        return this.street + ", " + this.houseNumber + ", " + this.town + ", " + this.postalCode;
    }
}
