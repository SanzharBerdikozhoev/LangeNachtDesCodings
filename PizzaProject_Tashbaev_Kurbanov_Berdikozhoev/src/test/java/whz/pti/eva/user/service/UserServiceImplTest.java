package whz.pti.eva.user.service;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import whz.pti.eva.customer.domain.Customer;
import whz.pti.eva.payment.service.PaymentService;
import whz.pti.eva.user.domain.*;
import whz.pti.eva.user.service.user.UserServiceImpl;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private PaymentService paymentService;


    @InjectMocks
    private UserServiceImpl userService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testFindByLoginNameFound() {
        User user = new User("johndoe", "hashedPassword");
        when(userRepository.findByLoginName("johndoe")).thenReturn(Optional.of(user));

        Optional<User> result = userService.findByLoginName("johndoe");

        assertTrue(result.isPresent());
        assertEquals("johndoe", result.get().getLoginName());
    }

    @Test
    public void testFindByLoginNameNotFound() {
        when(userRepository.findByLoginName("unknown")).thenReturn(Optional.empty());

        Optional<User> result = userService.findByLoginName("unknown");

        assertFalse(result.isPresent());
    }

    @Test
    public void testExistsByLoginNameTrue() {
        when(userRepository.existsByLoginName("johndoe")).thenReturn(true);

        boolean exists = userService.existsByLoginName("johndoe");

        assertTrue(exists);
    }

    @Test
    public void testExistsByLoginNameFalse() {
        when(userRepository.existsByLoginName("unknown")).thenReturn(false);

        boolean exists = userService.existsByLoginName("unknown");

        assertFalse(exists);
    }

    @Test
    public void testCreateUser() {
        UserCreateForm form = new UserCreateForm();
        form.setLoginName("johndoe");
        form.setPassword("password123");

        when(passwordEncoder.encode("password123")).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));


        doNothing().when(paymentService).createAccount("johndoe");
        User created = userService.create(form);

        assertNotNull(created);
        assertEquals("johndoe", created.getLoginName());
        assertEquals("hashedPassword", created.getPasswordHash());
        assertEquals(Role.USER, created.getRole());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    public void testCreate() {
        UserCreateForm form = new UserCreateForm();
        form.setLoginName("johndoe");
        form.setPassword("password123");

        when(passwordEncoder.encode("password123")).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        doNothing().when(paymentService).createAccount("johndoe");
        User created = userService.create(form);

        assertNotNull(created);
        assertEquals("johndoe", created.getLoginName());
        assertEquals("hashedPassword", created.getPasswordHash());

        verify(userRepository, times(1)).save(any(User.class));
    }

}

