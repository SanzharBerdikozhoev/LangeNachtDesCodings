package whz.pti.eva.user.service.user;

import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;
import whz.pti.eva.customer.domain.Customer;
import whz.pti.eva.payment.service.PaymentService;
import whz.pti.eva.user.domain.Role;
import whz.pti.eva.user.domain.User;
import whz.pti.eva.user.domain.UserCreateForm;
import whz.pti.eva.user.domain.UserRepository;

/**
 * Implementierung des UserService für Benutzeroperationen in der Pizza-Anwendung.
 * Bietet Methoden zum Suchen, Überprüfen und Erstellen von Benutzern mit Passwort-Hashing.
 */
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PaymentService paymentService;

    /**
     * Konstruktor, der UserRepository und PasswordEncoder injiziert.
     * @param userRepository Repository für Benutzerdaten
     * @param passwordEncoder Encoder für Passwörter
     */
    public UserServiceImpl(UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           PaymentService paymentService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.paymentService = paymentService;
    }

    /**
     * Findet einen Benutzer anhand seines Login-Namens.
     * @param username Login-Name des Benutzers
     * @return Optional mit dem User
     */
    @Override
    public Optional<User> findByLoginName(String username) {
        return userRepository.findByLoginName(username);
    }

    /**
     * Überprüft, ob ein Benutzer mit dem gegebenen Login-Namen existiert.
     * @param loginName Der zu prüfende Login-Name
     * @return true, falls der Benutzer existiert
     */
    @Override
    public boolean existsByLoginName(String loginName) {
        return userRepository.existsByLoginName(loginName);
    }

    /**
     * Erstellt einen neuen Benutzer aus dem Formular, hasht das Passwort und speichert ihn.
     * @param form Formular mit Benutzerdaten
     * @return Der erstellte User
     */
    @Override
    @Transactional
    public User create(UserCreateForm form) {
        User user = new User();
        user.setLoginName(form.getLoginName());
        user.setPasswordHash(passwordEncoder.encode(form.getPassword()));
        user.setRole(Role.USER);

        userRepository.save(user);
        paymentService.createAccount(user.getLoginName());

        return user;
    }
}

