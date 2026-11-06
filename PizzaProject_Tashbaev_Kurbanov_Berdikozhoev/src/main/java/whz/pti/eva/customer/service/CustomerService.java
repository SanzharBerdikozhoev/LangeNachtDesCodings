package whz.pti.eva.customer.service;

import whz.pti.eva.common.exceptions.CustomerNotFoundException;
import whz.pti.eva.customer.domain.Customer;
import whz.pti.eva.customer.domain.DeliveryAddress;
import whz.pti.eva.user.domain.User;

import java.util.List;

/**
 * Service-Schnittstelle für Kundenoperationen.
 * Definiert Methoden zum Abrufen, Erstellen, Aktualisieren und Blockieren von Kunden.
 */
public interface CustomerService {

    /**
     * Liefert eine Liste aller Kunden.
     *
     * @return Liste aller Kunden
     */
    List<Customer> getAll();

    /**
     * Erstellt einen neuen Kunden.
     *
     * @param firstName Vorname des Kunden
     * @param lastName  Nachname des Kunden
     * @throws IllegalArgumentException wenn loginName null ist
     */
    Customer create(String firstName, String lastName) throws IllegalArgumentException;

    /**
     * Aktualisiert die Daten eines bestehenden Kunden.
     *
     * @param id        ID des Kunden
     * @param firstName neuer Vorname
     * @param lastName  neuer Nachname
     * @throws CustomerNotFoundException wenn Kunde nicht gefunden
     */
    void update(Long id, String firstName, String lastName) throws CustomerNotFoundException;

    /**
     * Blockiert einen Kunden (setzt aktiv=false).
     *
     * @param id ID des Kunden
     * @throws CustomerNotFoundException wenn Kunde nicht gefunden
     */
    void block(Long id) throws CustomerNotFoundException;

    /**
     * Prüft, ob ein Benutzer (User-Objekt) bereits ein zugehöriges Customer-Profil besitzt.
     *
     * @param user Das User-Objekt.
     * @return true, wenn ein Kundenprofil existiert, sonst false.
     */
    boolean hasCustomer(User user);

    /**
     * Liefert das Customer-Objekt, das einem bestimmten User-Objekt zugeordnet ist.
     *
     * @param user Das User-Objekt.
     * @return Das gefundene Customer-Objekt.
     */
    Customer findByUser(User user);

    List<DeliveryAddress> getAddressesByCustomer(Customer customer);
}
