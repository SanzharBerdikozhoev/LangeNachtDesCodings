package whz.pti.eva.ordered.boundary;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import whz.pti.eva.cart.domain.Cart;
import whz.pti.eva.cart.domain.CartRepository;
import whz.pti.eva.cart.domain.Item;
import whz.pti.eva.cart.domain.ItemRepository;
import whz.pti.eva.menu.domain.Pizza;
import whz.pti.eva.menu.domain.PizzaRepository;
import whz.pti.eva.menu.domain.PizzaSize;
import whz.pti.eva.ordered.domain.Ordered;
import whz.pti.eva.ordered.domain.OrderedItem;
import whz.pti.eva.ordered.domain.OrderedRepository;
import whz.pti.eva.ordered.service.OrderedService;
import whz.pti.eva.payment.service.PaymentService;
import whz.pti.eva.user.domain.User;
import whz.pti.eva.user.service.currentuser.CurrentUserUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@SpringBootTest
@Transactional
public class OrderedControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @MockitoBean
    private OrderedService orderedService;

    @MockitoBean
    private OrderedRepository orderedRepository;

    @MockitoBean
    private CartRepository cartRepository;

    @MockitoBean
    private PizzaRepository pizzaRepository;

    @MockitoBean
    private ItemRepository itemRepository;

    @MockitoBean
    private PaymentService paymentService;


    private MockMvc mockMvc;

    private String testUserId = "42";

    private List<Ordered> preloadedOrders;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        Cart cart = new Cart();
        cart.setUserId(testUserId);

        List<Pizza> pizzas = List.of(
                new Pizza("Margherita",
                        new BigDecimal("9.99"),
                        new BigDecimal("7.99"),
                        new BigDecimal("5.99")),
                new Pizza("Pepperoni",
                        new BigDecimal("10.99"),
                        new BigDecimal("8.99"),
                        new BigDecimal("6.99")),
                new Pizza("Hawaiian",
                        new BigDecimal("11.99"),
                        new BigDecimal("9.99"),
                        new BigDecimal("7.99")),
                new Pizza("BBQ Chicken",
                        new BigDecimal("12.99"),
                        new BigDecimal("10.99"),
                        new BigDecimal("8.99"))
        );

        List<Item> items = List.of(
          new Item(10, PizzaSize.SMALL, pizzas.get(0), cart),
          new Item(3, PizzaSize.SMALL, pizzas.get(0), cart),
          new Item(4, PizzaSize.MEDIUM, pizzas.get(1), cart),
          new Item(1, PizzaSize.SMALL, pizzas.get(2), cart),
          new Item(3, PizzaSize.LARGE, pizzas.get(3), cart)
        );

        pizzaRepository.saveAll(pizzas);
        itemRepository.saveAll(items);

        cart.setItems(items);
        cartRepository.save(cart);

        preloadedOrders = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            Ordered o = new Ordered();
            o.setNumberOfItems(i);
            o.setTotalPrice(BigDecimal.valueOf(i * 10));

            List<OrderedItem> orderedItems = new ArrayList<>();
            for (int j = 0; j < items.size(); j++) {
                Item cartItem = items.get(j);
                OrderedItem oi = new OrderedItem();
                oi.setName(cartItem.getPizza().getName());
                oi.setQuantity(cartItem.getQuantity());
                oi.setPrice(cartItem.getPizza().getPriceMedium().multiply(BigDecimal.valueOf(cartItem.getQuantity())));
                oi.setSize(cartItem.getSize());
                orderedItems.add(oi);
            }

            o.setItems(orderedItems);
            orderedRepository.save(o);
            preloadedOrders.add(o);
        }
    }

    @Test
    public void testGetAllOrdered() throws Exception {
        try (MockedStatic<CurrentUserUtils> mockedStatic =
                     Mockito.mockStatic(CurrentUserUtils.class)) {

            User user = new User();
            user.setLoginName(testUserId);

            mockedStatic.when(CurrentUserUtils::getUser)
                    .thenReturn(Optional.of(user));

            when(paymentService.getBalance(testUserId)).thenReturn(1000.0);

            mockMvc.perform(get("/ordered"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("ordered/ordered"))
                    .andExpect(model().attributeExists("ordered"))
                    .andDo(print());
        }
    }


    @Test
    public void testAddOrdered() throws Exception {

        User user = new User();
        user.setLoginName(testUserId);

        try (MockedStatic<CurrentUserUtils> mocked =
                     Mockito.mockStatic(CurrentUserUtils.class)) {

            mocked.when(CurrentUserUtils::getUser)
                    .thenReturn(Optional.of(user));

            when(orderedService.addOrdered(
                    eq(testUserId),
                    eq(List.of(1L, 2L)),
                    eq(42L)
            )).thenReturn(1L);

            when(paymentService.getBalance(testUserId)).thenReturn(1000.0);

            mockMvc.perform(post("/ordered")
                            .param("selectedItems", "1", "2")
                            .param("deliveryAddressId", "42")
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/ordered"))
                    .andDo(print());

            verify(orderedService).addOrdered(
                    testUserId,
                    List.of(1L, 2L),
                    42L
            );
        }
    }

    @Test
    public void testDeleteOrderedByObject() throws Exception {
        Ordered orderToDelete = preloadedOrders.get(0);

        doNothing().when(orderedService).deleteOrdered(any(Ordered.class));

        mockMvc.perform(delete("/ordered")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("id", String.valueOf(orderToDelete.getId()))
                        .param("numberOfItems", String.valueOf(orderToDelete.getNumberOfItems()))
                        .param("totalPrice", String.valueOf(orderToDelete.getTotalPrice())))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andDo(print());

        verify(orderedService, times(1)).deleteOrdered(any(Ordered.class));
    }
}
