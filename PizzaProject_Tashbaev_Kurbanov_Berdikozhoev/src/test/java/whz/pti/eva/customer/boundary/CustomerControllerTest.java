package whz.pti.eva.customer.boundary;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import whz.pti.eva.customer.domain.Customer;
import whz.pti.eva.user.domain.User;
import whz.pti.eva.customer.service.CustomerService;

import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
public class CustomerControllerTest {

    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;

    @MockitoBean
    private CustomerService customerService;

    private final String firstName = "Baitur";
    private final String lastName = "Tashbaev";
    private final String loginName = "Tashbaevvb";
    private final String password = "1234";

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }

    @Test
    void listCustomers_returnsCustomerListView() throws Exception {
        String loginName1 = loginName;
        User user1 = new User(loginName1, password);
        Customer customer1 = new Customer(firstName, lastName, user1);
        user1.setCustomer(customer1);

        String loginName2 = loginName + "1";
        User user2 = new User(loginName2, password);
        Customer customer2 = new Customer(firstName + "1", lastName, user2);
        user2.setCustomer(customer2);

        List<Customer> mockCustomers = List.of(customer1, customer2);

        when(customerService.getAll()).thenReturn(mockCustomers);

        mockMvc.perform(get("/customers"))
                .andExpect(status().isOk())
                .andExpect(view().name("customer/customers"))
                .andExpect(model().attributeExists("customers"))
                .andExpect(model().attributeExists("newCustomer"));
    }

    @Test
    void createCustomer_redirectsToCustomers() throws Exception {
        mockMvc.perform(post("/customers/create")
                        .param("firstName", firstName)
                        .param("lastName", lastName))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/customers"));

        verify(customerService).create(eq(firstName), eq(lastName));
    }

    @Test
    void blockCustomer_blocksAndRedirects() throws Exception {
        mockMvc.perform(post("/customers/7/block"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/customers"));

        verify(customerService).block(7L);
    }
}