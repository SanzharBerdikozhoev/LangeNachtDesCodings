package whz.pti.eva.customer.boundary;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import whz.pti.eva.customer.domain.Customer;
import whz.pti.eva.customer.domain.DeliveryAddress;
import whz.pti.eva.customer.domain.DeliveryAddressForm;
import whz.pti.eva.customer.service.DeliveryAddressService;
import whz.pti.eva.user.domain.User;
import whz.pti.eva.user.service.currentuser.CurrentUserUtils;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
public class DeliveryAddressControllerTest {

    @Mock
    private DeliveryAddressService deliveryAddressService;

    private MockMvc mockMvc;
    private DeliveryAddressController controller;

    @BeforeEach
    void setup() {
        controller = new DeliveryAddressController(deliveryAddressService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new ExceptionHandlerControllerAdvice())
                .build();
    }

    @ControllerAdvice
    static class ExceptionHandlerControllerAdvice {
        @ExceptionHandler({
                org.springframework.security.core.userdetails.UsernameNotFoundException.class,
                IllegalStateException.class
        })
        public String handleSecurityExceptions(Exception ex) {
            return "redirect:/profile";
        }
    }

    private User createUserWithCustomer(Long customerId) {
        User user = new User("login", "pass");
        Customer customer = new Customer("First", "Last", user);
        user.setCustomer(customer);
        if (customerId != null) {
            try {
                var field = customer.getClass().getSuperclass().getDeclaredField("id");
                field.setAccessible(true);
                field.set(customer, customerId);
            } catch (Exception ignored) {
            }
        }
        return user;
    }

    @Test
    void givenValidationErrors_whenAddAddress_thenRedirectToProfile() throws Exception {
        mockMvc.perform(post("/address/add")
                        .param("street", "")
                        .param("houseNumber", "")
                        .param("town", "")
                        .param("postalCode", ""))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile"))
                .andExpect(flash().attributeExists("form"))
                .andExpect(flash().attributeExists("org.springframework.validation.BindingResult.form"));
    }

    @Test
    void givenNoCurrentUser_whenAddAddress_thenThrowException_handledAsRedirect() throws Exception {
        try (MockedStatic<CurrentUserUtils> mocked = mockStatic(CurrentUserUtils.class)) {
            mocked.when(CurrentUserUtils::getUser).thenReturn(Optional.empty());

            DeliveryAddressForm form = new DeliveryAddressForm();
            form.setStreet("Street");
            form.setHouseNumber("10");
            form.setTown("Town");
            form.setPostalCode("12345");

            mockMvc.perform(post("/address/add")
                            .flashAttr("form", form))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/profile"));
        }
    }

    @Test
    void givenUserHasNoCustomer_whenAddAddress_thenThrowIllegalStateException_handled() throws Exception {
        try (MockedStatic<CurrentUserUtils> mocked = mockStatic(CurrentUserUtils.class)) {
            User user = new User("login", "pass");
            mocked.when(CurrentUserUtils::getUser).thenReturn(Optional.of(user));

            DeliveryAddressForm form = new DeliveryAddressForm();
            form.setStreet("Street");
            form.setHouseNumber("10");
            form.setTown("Town");
            form.setPostalCode("12345");

            mockMvc.perform(post("/address/add")
                            .flashAttr("form", form))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/profile"));
        }
    }

    @Test
    void givenValidFormAndUser_whenAddAddress_thenSaveAndRedirect() throws Exception {
        try (MockedStatic<CurrentUserUtils> mocked = mockStatic(CurrentUserUtils.class)) {
            User user = createUserWithCustomer(5L);
            mocked.when(CurrentUserUtils::getUser).thenReturn(Optional.of(user));

            DeliveryAddressForm form = new DeliveryAddressForm();
            form.setStreet("Street");
            form.setHouseNumber("10");
            form.setTown("Town");
            form.setPostalCode("12345");

            mockMvc.perform(post("/address/add")
                            .flashAttr("form", form))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/profile"))
                    .andExpect(flash().attribute("success", "Address wurde erstellt."));

            verify(deliveryAddressService).save(any(DeliveryAddress.class), eq(5L));
        }
    }

    @Test
    void givenRedirectUrl_whenAddAddress_thenRedirectToCustomUrl() throws Exception {
        try (MockedStatic<CurrentUserUtils> mocked = mockStatic(CurrentUserUtils.class)) {
            User user = createUserWithCustomer(10L);
            mocked.when(CurrentUserUtils::getUser).thenReturn(Optional.of(user));

            DeliveryAddressForm form = new DeliveryAddressForm();
            form.setStreet("Street");
            form.setHouseNumber("10");
            form.setTown("Town");
            form.setPostalCode("12345");

            mockMvc.perform(post("/address/add")
                            .param("redirectUrl", "/checkout")
                            .flashAttr("form", form))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/checkout"));

            verify(deliveryAddressService).save(any(), eq(10L));
        }
    }
}