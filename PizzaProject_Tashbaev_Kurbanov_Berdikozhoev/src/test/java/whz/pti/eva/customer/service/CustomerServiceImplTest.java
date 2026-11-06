package whz.pti.eva.customer.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import whz.pti.eva.common.exceptions.CustomerNotFoundException;
import whz.pti.eva.customer.domain.Customer;
import whz.pti.eva.customer.domain.CustomerRepository;
import whz.pti.eva.user.domain.User;
import whz.pti.eva.user.service.currentuser.CurrentUserUtils;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
public class CustomerServiceImplTest {

    @Mock
    private CustomerRepository customerRepository;

    private CustomerService customerService;

    private final String firstName = "Baitur";
    private final String lastName = "Tashbaev";
    private final String loginName = "Tashbaevvb";
    private final String password = "1234";

    @BeforeEach
    void setup() {
        customerService = new CustomerServiceImpl(customerRepository);
    }

    @Test
    void givenNoCustomers_whenGetAll_thenReturnEmptyList() {
        when(customerRepository.findAll()).thenReturn(Collections.emptyList());

        List<Customer> result = customerService.getAll();

        assertTrue(result.isEmpty());
    }

    @Test
    void givenCustomersExist_whenGetAll_thenReturnCustomerList() {
        List<Customer> mockList = List.of(
                createMockCustomer(firstName, lastName, loginName),
                createMockCustomer("C", "D", "CD")
        );
        when(customerRepository.findAll()).thenReturn(mockList);

        List<Customer> result = customerService.getAll();

        assertEquals(2, result.size());
        assertEquals(loginName, result.get(0).getUser().getLoginName());
    }

    private Customer createMockCustomer(String fn, String ln, String log) {
        User u = new User(log, password);
        Customer c = new Customer(fn, ln, u);
        u.setCustomer(c);
        return c;
    }

    @Test
    void givenValidInput_whenCreateCustomer_thenCustomerIsSaved() {
        try (MockedStatic<CurrentUserUtils> mockedStatic = mockStatic(CurrentUserUtils.class)) {
            User mockUser = new User(loginName, password);
            mockedStatic.when(CurrentUserUtils::getUser).thenReturn(Optional.of(mockUser));

            customerService.create(firstName, lastName);

            ArgumentCaptor<Customer> captor = ArgumentCaptor.forClass(Customer.class);
            verify(customerRepository).save(captor.capture());

            Customer saved = captor.getValue();
            assertEquals(firstName, saved.getFirstName());
            assertEquals(lastName, saved.getLastName());
            assertEquals(mockUser, saved.getUser());
            assertEquals(saved, mockUser.getCustomer());
        }
    }

    @Test
    void givenNoCurrentUser_whenCreateCustomer_thenThrowIllegalArgumentException() {
        try (MockedStatic<CurrentUserUtils> mockedStatic = mockStatic(CurrentUserUtils.class)) {
            mockedStatic.when(CurrentUserUtils::getUser).thenReturn(Optional.empty());

            assertThrows(IllegalArgumentException.class,
                    () -> customerService.create(firstName, lastName)
            );
        }
    }

    @Test
    void givenSaveFails_whenCreateCustomer_thenThrowException() {
        try (MockedStatic<CurrentUserUtils> mockedStatic = mockStatic(CurrentUserUtils.class)) {
            User mockUser = new User(loginName, password);
            mockedStatic.when(CurrentUserUtils::getUser).thenReturn(Optional.of(mockUser));
            when(customerRepository.save(any())).thenThrow(new RuntimeException("DB Fehlgeschlagen"));

            assertThrows(RuntimeException.class,
                    () -> customerService.create(firstName, lastName)
            );
        }
    }

    @Test
    void givenExistingCustomer_whenUpdateCustomer_thenFieldsAreUpdated() {
        Customer customer = createMockCustomer(firstName, lastName, loginName);
        when(customerRepository.findById(10L)).thenReturn(Optional.of(customer));

        customerService.update(10L, "NewFirst", "NewLast");

        assertEquals("NewFirst", customer.getFirstName());
        assertEquals("NewLast", customer.getLastName());
    }

    @Test
    void givenNotExistCustomer_whenUpdateCustomer_thenThrowCustomerNotFoundException() {
        when(customerRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(CustomerNotFoundException.class,
                () -> customerService.update(999L, firstName, lastName)
        );
    }

    @Test
    void givenCustomerLoaded_whenUpdateCustomer_thenRepositorySaveIsNotCalled() {
        Customer customer = createMockCustomer(firstName, lastName, loginName);
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));

        customerService.update(1L, "NewFirst", "NewLast");

        verify(customerRepository, never()).save(any());
    }

    @Test
    void givenActiveCustomer_whenBlockCustomer_thenCustomerBecomesInactive() {
        Customer c = createMockCustomer(firstName, lastName, loginName);
        when(customerRepository.findById(5L)).thenReturn(Optional.of(c));

        customerService.block(5L);

        assertFalse(c.isActive());
        verify(customerRepository, times(1)).findById(5L);
    }

    @Test
    void givenInactiveCustomer_whenBlockCustomer_thenCustomerStaysInactive() {
        Customer c = createMockCustomer(firstName, lastName, loginName);
        c.setActive(false);
        when(customerRepository.findById(7L)).thenReturn(Optional.of(c));

        customerService.block(7L);

        assertFalse(c.isActive());
    }

    @Test
    void givenCustomerNotExists_whenBlockCustomer_thenThrowCustomerNotFoundException() {
        when(customerRepository.findById(123L)).thenReturn(Optional.empty());

        assertThrows(CustomerNotFoundException.class,
                () -> customerService.block(123L)
        );
    }
}