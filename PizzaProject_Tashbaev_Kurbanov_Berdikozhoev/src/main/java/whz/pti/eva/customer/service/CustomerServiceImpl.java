package whz.pti.eva.customer.service;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import whz.pti.eva.common.exceptions.CustomerNotFoundException;
import whz.pti.eva.customer.domain.Customer;
import whz.pti.eva.customer.domain.CustomerRepository;
import whz.pti.eva.customer.domain.DeliveryAddress;
import whz.pti.eva.user.domain.User;
import whz.pti.eva.user.service.currentuser.CurrentUserUtils;

import java.util.List;
import java.util.Optional;

/**
 * Implementierung des CustomerService.
 * Verwaltet Kunden in der Datenbank mit den Methoden: Abrufen, Erstellen, Aktualisieren und Blockieren.
 */
@Service
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;

    /**
     * Konstruktor für CustomerServiceImpl injiziert folgende Abhängigkeiten.
     *
     * @param customerRepository Repository für Kunden
     */
    public CustomerServiceImpl(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    /**
     * Liefert alle Kunden.
     *
     * @return Liste aller Kunden
     */
    @Override
    public List<Customer> getAll() {
        return customerRepository.findAll();
    }

    /**
     * Erstellt einen neuen Kunden mit temporärem Passwort.
     *
     * @param firstName Vorname
     * @param lastName  Nachname
     * @throws IllegalArgumentException wenn loginName null ist
     * @return customer Erstellte Kunde
     */
    @Override
    @Transactional
    public Customer create(String firstName, String lastName) throws IllegalArgumentException {
        Optional<User> currentUser = CurrentUserUtils.getUser();

        if (currentUser.isEmpty()) {
            throw new IllegalArgumentException("Nicht autorisiert. Bitte melden Sie sich an.");
        }

        Customer customer = new Customer(firstName, lastName, currentUser.get());
        currentUser.get().setCustomer(customer);

        return customerRepository.save(customer);
    }

    /**
     * Aktualisiert einen bestehenden Kunden.
     *
     * @param id           ID des Kunden
     * @param newFirstName neuer Vorname
     * @param newLastName  neuer Nachname
     * @throws CustomerNotFoundException wenn Kunde nicht gefunden wird
     */
    @Override
    @Transactional
    public void update(Long id, String newFirstName, String newLastName) throws CustomerNotFoundException {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException(id));

        customer.setFirstName(newFirstName);
        customer.setLastName(newLastName);
    }

    /**
     * Blockiert einen Kunden.
     *
     * @param id ID des Kunden
     * @throws CustomerNotFoundException wenn Kunde nicht gefunden wird
     */
    @Override
    @Transactional
    public void block(Long id) throws CustomerNotFoundException {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException(id));
        customer.setActive(false);
    }

    @Override
    public boolean hasCustomer(User user) {
        return user.getCustomer() != null;
    }

    @Override
    public Customer findByUser(User user) {
        return customerRepository.findByUserWithAddresses(user);
    }

    @Override
    public List<DeliveryAddress> getAddressesByCustomer(Customer customer) {
        return customerRepository.findByUserWithAddresses(customer.getUser()).getDeliveryAddresses();
    }
}
