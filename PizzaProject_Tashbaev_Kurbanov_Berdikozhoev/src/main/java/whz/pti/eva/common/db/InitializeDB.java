package whz.pti.eva.common.db;

import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import whz.pti.eva.customer.domain.Customer;
import whz.pti.eva.customer.domain.CustomerRepository;
import whz.pti.eva.customer.domain.DeliveryAddress;
import whz.pti.eva.customer.domain.DeliveryAddressRepository;
import whz.pti.eva.user.domain.Role;
import whz.pti.eva.user.domain.User;
import whz.pti.eva.user.domain.UserRepository;

import java.util.List;

/**
 * Diese Komponente initialisiert die Datenbank mit vordefinierten Benutzern für die Pizza-Anwendung.
 * Sie erstellt Admin- und User-Benutzer bei Anwendungsstart, falls sie nicht existieren.
 */
@Component
public class InitializeDB {

    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final DeliveryAddressRepository deliveryAddressRepository;


    /**
     * Konstruktor, der das UserRepository injiziert.
     * @param userRepository das Repository für Benutzerdaten
     */
    @Autowired
    public InitializeDB(UserRepository userRepository, CustomerRepository customerRepository, DeliveryAddressRepository deliveryAddressRepository) {
        this.userRepository = userRepository;
        this.customerRepository = customerRepository;
        this.deliveryAddressRepository = deliveryAddressRepository;
    }

    /**
     * Initialisiert die Datenbank nach der Erstellung der Bean.
     * Erstellt vordefinierte Benutzer mit verschlüsselten Passwörtern.
     */
    @PostConstruct
    public void init() {
        PasswordEncoder passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();

        User admin = createUserIfNotExists("admin", "a1", Role.ADMIN, passwordEncoder);
        User user1 = createUserIfNotExists("bnutz", "n1", Role.USER, passwordEncoder);
        User user2 = createUserIfNotExists("cnutz", "n2", Role.USER, passwordEncoder);

        Customer profile1 = addProfileToUser("anonym", "anonym", user1);
        addDeliveryAddressToProfile("Süderstraße", "131", "Hamburg", "20097", profile1);
        addDeliveryAddressToProfile("Moselstraße", "30", "Frankfurt am Main", "60326", profile1);
    }

    @Transactional
    public DeliveryAddress addDeliveryAddressToProfile(
            String street,
            String houseNumber,
            String town,
            String postalCode,
            Customer profile
    ) {
        boolean exists = profile.getDeliveryAddresses().stream()
                .anyMatch(a -> a.getStreet().equals(street)
                        && a.getHouseNumber().equals(houseNumber)
                        && a.getTown().equals(town)
                        && a.getPostalCode().equals(postalCode));

        if (exists) {
            return profile.getDeliveryAddresses().stream()
                    .filter(a -> a.getStreet().equals(street)
                            && a.getHouseNumber().equals(houseNumber)
                            && a.getTown().equals(town)
                            && a.getPostalCode().equals(postalCode))
                    .findFirst()
                    .get();
        }

        DeliveryAddress deliveryAddress = new DeliveryAddress();
        deliveryAddress.setStreet(street);
        deliveryAddress.setHouseNumber(houseNumber);
        deliveryAddress.setTown(town);
        deliveryAddress.setPostalCode(postalCode);

        deliveryAddress.getCustomers().add(profile);
        profile.getDeliveryAddresses().add(deliveryAddress);

        deliveryAddressRepository.save(deliveryAddress);

        return deliveryAddress;
    }


    private Customer addProfileToUser(String firstName, String lastName, User user) {
        Customer customer = new Customer();
        customer.setFirstName(firstName);
        customer.setLastName(lastName);
        customer.setUser(user);

        user.setCustomer(customer);
        customerRepository.save(customer);
        userRepository.save(user);

        return customer;
    }

    /**
     * Erstellt einen Benutzer, falls er nicht existiert.
     * Verschlüsselt das Passwort und speichert den Benutzer.
     * @param loginName Benutzername
     * @param rawPassword Roh-Passwort
     * @param role Rolle des Benutzers
     * @param encoder Passwort-Encoder
     */
    private User createUserIfNotExists(String loginName, String rawPassword, Role role, PasswordEncoder encoder) {
        if (userRepository.existsByLoginName(loginName)) {
            return null;
        }

        User user = new User();
        user.setLoginName(loginName);
        user.setPasswordHash(encoder.encode(rawPassword));
        user.setRole(role);

        userRepository.save(user);
        return user;
    }
}
