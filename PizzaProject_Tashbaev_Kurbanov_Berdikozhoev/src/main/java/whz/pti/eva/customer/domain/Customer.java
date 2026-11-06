package whz.pti.eva.customer.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToOne;
import whz.pti.eva.common.domain.BaseEntity;
import whz.pti.eva.user.domain.User;

import java.util.ArrayList;
import java.util.List;

/**
 * Entität zur Speicherung von Kundeninformationen und der Verknüpfung mit dem Benutzerkonto (User).
 * <p>
 * Speichert den Vor- und Nachnamen und verwaltet die zugehörigen Lieferadressen.
 * Entspricht der Anforderung aus Teil 2 der Projektarbeit.
 */
@Entity
public class Customer extends BaseEntity<Long> {

    private String firstName, lastName;

    private boolean active = true;

    /**
     * Eindeutige Eins-zu-Eins-Beziehung zum Benutzerkonto (User).
     * Wird faul geladen (FetchType.LAZY) und ist zwingend erforderlich (nullable=false, unique=true).
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;


    /**
     * Konstruktor zur Erstellung eines neuen Customer-Objekts.
     *
     * @param firstName Vorname.
     * @param lastName Nachname.
     * @param user Das zugehörige User-Objekt.
     */
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "customer_delivery_address",
            joinColumns = @JoinColumn(name = "customer_id"),
            inverseJoinColumns = @JoinColumn(name = "delivery_address_id")
    )
    private List<DeliveryAddress> deliveryAddresses = new ArrayList<>();


    public Customer(String firstName, String lastName, User user) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.user = user;
    }

    public Customer() {
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

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

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public List<DeliveryAddress> getDeliveryAddresses() {
        return this.deliveryAddresses;
    }

    /**
     * Fügt eine Lieferadresse hinzu und stellt die bidirektionale Verknüpfung her.
     *
     * @param address Die hinzuzufügende Adresse.
     */
    public void addDeliveryAddress(DeliveryAddress address) {
        deliveryAddresses.add(address);
        address.getCustomers().add(this);
    }

    /**
     * Entfernt eine Lieferadresse und löst die bidirektionale Verknüpfung auf.
     *
     * @param address Die zu entfernende Adresse.
     */
    public void removeDeliveryAddress(DeliveryAddress address) {
        deliveryAddresses.remove(address);
        address.getCustomers().remove(this);
    }

    /**
     * Gibt den Login-Namen des zugehörigen Benutzers zurück.
     *
     * @return Der Login-Name oder null, falls kein User zugeordnet ist.
     */
    public String getLoginName() {
        return user != null ? user.getLoginName() : null;
    }
}
