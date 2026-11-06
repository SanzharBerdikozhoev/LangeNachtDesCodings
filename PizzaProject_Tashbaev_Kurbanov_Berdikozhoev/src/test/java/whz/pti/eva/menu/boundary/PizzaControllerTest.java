package whz.pti.eva.menu.boundary;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import whz.pti.eva.cart.service.CartService;
import whz.pti.eva.menu.domain.Pizza;
import whz.pti.eva.menu.service.PizzaService;
import whz.pti.eva.user.domain.User;
import whz.pti.eva.user.service.currentuser.CurrentUserUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
class PizzaControllerTest {

    @Mock private PizzaService pizzaService;
    @Mock private CartService cartService;

    private MockMvc mockMvc;
    private PizzaController controller;

    @BeforeEach
    void setup() {
        controller = new PizzaController(pizzaService, cartService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void getAllPizza_anonymousUser_showsSessionCart() throws Exception {
        try (MockedStatic<CurrentUserUtils> utils = mockStatic(CurrentUserUtils.class)) {
            utils.when(CurrentUserUtils::getUser).thenReturn(Optional.empty());

            when(pizzaService.searchByName("")).thenReturn(List.of(
                    new Pizza("Margherita", BigDecimal.TEN, BigDecimal.TEN, BigDecimal.TEN)
            ));
            when(cartService.getSessionItems()).thenReturn(List.of());

            mockMvc.perform(get("/"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("pizza/pizza-menu"))
                    .andExpect(model().attributeExists("pizzas"))
                    .andExpect(model().attributeExists("sessionCart"));

            verify(cartService).getSessionItems();
            verify(cartService, never()).mergeSessionCartIntoDb(any());
        }
    }

    @Test
    void getAllPizza_loggedInUser_mergesSessionCartAndShowsDbCart() throws Exception {
        try (MockedStatic<CurrentUserUtils> utils = mockStatic(CurrentUserUtils.class)) {
            User user = mock(User.class);
            when(user.getLoginName()).thenReturn("baitur");
            utils.when(CurrentUserUtils::getUser).thenReturn(Optional.of(user));

            when(pizzaService.searchByName("")).thenReturn(List.of(
                    new Pizza("Pepperoni", BigDecimal.valueOf(12), BigDecimal.valueOf(12), BigDecimal.valueOf(12))
            ));
            when(cartService.findByUserId("baitur")).thenReturn(new whz.pti.eva.cart.domain.Cart());

            mockMvc.perform(get("/"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("pizza/pizza-menu"))
                    .andExpect(model().attributeExists("pizzas"))
                    .andExpect(model().attributeExists("cart"));

            verify(cartService).mergeSessionCartIntoDb("baitur");
            verify(cartService).findByUserId("baitur");
        }
    }

    @Test
    void showPizzaManagement_returnsManagementView() throws Exception {
        when(pizzaService.listAll()).thenReturn(List.of());

        mockMvc.perform(get("/pizzas"))
                .andExpect(status().isOk())
                .andExpect(view().name("pizza/pizza-management"))
                .andExpect(model().attributeExists("pizzas"))
                .andExpect(model().attributeExists("newPizza"));

        verify(pizzaService).listAll();
    }

    @Test
    void addPizza_savesAndRedirects() throws Exception {
        mockMvc.perform(post("/pizzas/add")
                        .param("name", "Quattro Formaggi")
                        .param("priceSmall", "8")
                        .param("priceMedium", "10")
                        .param("priceLarge", "12"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/pizzas"));

        verify(pizzaService).addPizza(argThat(pizza ->
                "Quattro Formaggi".equals(pizza.getName()) &&
                        BigDecimal.valueOf(8).equals(pizza.getPriceSmall()) &&
                        BigDecimal.valueOf(10).equals(pizza.getPriceMedium()) &&
                        BigDecimal.valueOf(12).equals(pizza.getPriceLarge())
        ));
    }

    @Test
    void editPizzaPage_showsEditForm() throws Exception {
        Pizza pizza = new Pizza("Hawaii", BigDecimal.valueOf(11), BigDecimal.valueOf(11), BigDecimal.valueOf(11));
        when(pizzaService.getById(5L)).thenReturn(Optional.of(pizza));

        mockMvc.perform(get("/pizzas/edit/{id}", 5L))
                .andExpect(status().isOk())
                .andExpect(view().name("pizza/pizza-edit"))
                .andExpect(model().attribute("pizza", pizza));

        verify(pizzaService).getById(5L);
    }

    @Test
    void updatePizza_updatesAndRedirects() throws Exception {
        mockMvc.perform(post("/pizzas/update/{id}", 3L)
                        .param("name", "Updated Pizza")
                        .param("priceSmall", "7")
                        .param("priceMedium", "9")
                        .param("priceLarge", "11"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/pizzas"));

        verify(pizzaService).updateById(eq(3L), any(Pizza.class));
    }

    @Test
    void deletePizza_deletesAndRedirects() throws Exception {
        mockMvc.perform(get("/pizzas/delete/{id}", 7L))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/pizzas"));

        verify(pizzaService).deleteById(7L);
    }
}
