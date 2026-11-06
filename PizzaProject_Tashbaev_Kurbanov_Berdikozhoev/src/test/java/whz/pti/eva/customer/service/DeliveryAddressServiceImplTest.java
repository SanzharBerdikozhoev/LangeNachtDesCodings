package whz.pti.eva.customer.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import whz.pti.eva.customer.domain.Customer;
import whz.pti.eva.customer.domain.CustomerRepository;
import whz.pti.eva.customer.domain.DeliveryAddress;
import whz.pti.eva.customer.domain.DeliveryAddressRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
public class DeliveryAddressServiceImplTest {

    @Mock
    private DeliveryAddressRepository addressRepository;

    @Mock
    private CustomerRepository customerRepository;

    private DeliveryAddressService deliveryAddressService;

    @BeforeEach
    void setup() {
        deliveryAddressService = new DeliveryAddressServiceImpl(addressRepository, customerRepository);
    }

    private DeliveryAddress createAddress() {
        return new DeliveryAddress("Street", "10A", "Town", "12345");
    }

    private Customer createCustomer(Long id) {
        Customer c = new Customer("A", "B", null);
        c.setIdForTest(id);
        return c;
    }

    @Test
    void givenCustomerExistsAndNewAddress_whenSave_thenSaveAddressAndLinkToCustomer() {
        DeliveryAddress newAddress = createAddress();
        Customer customer = createCustomer(5L);

        when(customerRepository.findById(5L)).thenReturn(Optional.of(customer));
        when(addressRepository.findByStreetAndHouseNumberAndTownAndPostalCodeAllIgnoreCase(
                any(), any(), any(), any()
        )).thenReturn(Optional.empty());

        when(addressRepository.save(any())).thenReturn(newAddress);

        DeliveryAddress result = deliveryAddressService.save(newAddress, 5L);

        assertEquals(newAddress, result);
        assertTrue(customer.getDeliveryAddresses().contains(newAddress));
        assertTrue(newAddress.getCustomers().contains(customer));

        verify(addressRepository, times(1)).save(newAddress);
        verify(customerRepository, times(1)).save(customer);
    }


    @Test
    void givenCustomerExistsAndAddressAlreadyExists_whenSave_thenDoNotCreateNewAddress() {
        DeliveryAddress existing = createAddress();
        Customer customer = createCustomer(8L);

        when(customerRepository.findById(8L)).thenReturn(Optional.of(customer));
        when(addressRepository.findByStreetAndHouseNumberAndTownAndPostalCodeAllIgnoreCase(
                any(), any(), any(), any()
        )).thenReturn(Optional.of(existing));

        DeliveryAddress result = deliveryAddressService.save(existing, 8L);

        assertEquals(existing, result);
        assertTrue(existing.getCustomers().contains(customer));

        verify(addressRepository, never()).save(any());
        verify(customerRepository, times(1)).save(customer);
    }

    @Test
    void givenCustomerDoesNotExist_whenSave_thenThrowException() {
        DeliveryAddress addr = createAddress();

        when(customerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> deliveryAddressService.save(addr, 99L)
        );
    }


    @Test
    void givenAddressExistsAndCustomerOwnsIt_whenUpdateToNewAddress_thenCreateAndLinkNewAddress() {
        DeliveryAddress existing = createAddress();
        Customer customer = createCustomer(1L);
        existing.getCustomers().add(customer);
        customer.getDeliveryAddresses().add(existing);

        DeliveryAddress updated = new DeliveryAddress("NewStreet", "11B", "NewTown", "54321");

        when(addressRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(addressRepository.findByStreetAndHouseNumberAndTownAndPostalCodeAllIgnoreCase(
                any(), any(), any(), any())).thenReturn(Optional.empty());
        when(addressRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        deliveryAddressService.update(1L, updated, 1L);

        assertTrue(customer.getDeliveryAddresses().stream()
                .anyMatch(addr -> addr.getStreet().equals("NewStreet")));
        assertFalse(customer.getDeliveryAddresses().contains(existing));

        verify(addressRepository, times(1)).save(any(DeliveryAddress.class));
        verify(customerRepository, times(1)).save(customer);

    }

    @Test
    void givenAddressExistsAndCustomerOwnsIt_whenUpdateToExistingAddress_thenLinkExistingAddress() {
        DeliveryAddress existing = createAddress();
        Customer customer = createCustomer(2L);
        existing.getCustomers().add(customer);
        customer.getDeliveryAddresses().add(existing);

        DeliveryAddress duplicate = new DeliveryAddress("Street", "10A", "Town", "12345");

        when(addressRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(customerRepository.findById(2L)).thenReturn(Optional.of(customer));
        when(addressRepository.findByStreetAndHouseNumberAndTownAndPostalCodeAllIgnoreCase(
                any(), any(), any(), any())).thenReturn(Optional.of(duplicate));

        deliveryAddressService.update(1L, duplicate, 2L);

        assertTrue(customer.getDeliveryAddresses().contains(duplicate));
        assertFalse(customer.getDeliveryAddresses().contains(existing));

        verify(addressRepository, never()).save(any());
        verify(customerRepository, times(1)).save(customer);

    }

    @Test
    void givenCustomerDoesNotOwnAddress_whenUpdate_thenThrowException() {
        DeliveryAddress existing = createAddress();
        Customer customer = createCustomer(3L);

        when(addressRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(customerRepository.findById(3L)).thenReturn(Optional.of(customer));

        DeliveryAddress updated = createAddress();

        assertThrows(IllegalArgumentException.class,
                () -> deliveryAddressService.update(1L, updated, 3L));

    }

    @Test
    void givenAddressNotExist_whenUpdate_thenThrowException() {
        DeliveryAddress updated = createAddress();

        when(addressRepository.findById(999L)).thenReturn(Optional.empty());
        when(customerRepository.findById(1L)).thenReturn(Optional.of(createCustomer(1L)));

        assertThrows(RuntimeException.class,
                () -> deliveryAddressService.update(999L, updated, 1L));

    }

    @Test
    void givenCustomerNotExist_whenUpdate_thenThrowException() {
        DeliveryAddress existing = createAddress();
        DeliveryAddress updated = createAddress();

        when(addressRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(customerRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> deliveryAddressService.update(1L, updated, 999L));

    }


    @Test
    void givenAddressNotExists_whenDelete_thenThrowException() {
        when(addressRepository.findById(100L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> deliveryAddressService.delete(100L, 5L)
        );
    }

    @Test
    void givenCustomerNotExists_whenDelete_thenThrowException() {
        DeliveryAddress addr = createAddress();

        when(addressRepository.findById(5L)).thenReturn(Optional.of(addr));
        when(customerRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> deliveryAddressService.delete(5L, 999L)
        );
    }
}
