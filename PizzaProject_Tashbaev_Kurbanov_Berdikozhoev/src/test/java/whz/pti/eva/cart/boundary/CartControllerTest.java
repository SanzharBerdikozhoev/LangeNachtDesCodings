package whz.pti.eva.cart.boundary;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import whz.pti.eva.cart.domain.Cart;
import whz.pti.eva.cart.service.CartService;
import whz.pti.eva.customer.service.CustomerService;
import whz.pti.eva.menu.domain.PizzaSize;
import whz.pti.eva.user.domain.CurrentUser;
import whz.pti.eva.user.domain.User;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
public class CartControllerTest {

    @Mock
    private CartService cartService;

    @Mock
    private CustomerService customerService;

    private MockMvc mockMvc;
    private CartController controller;

    @BeforeEach
    void setup() {
        controller = new CartController(cartService, customerService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void givenLoggedInUser_whenViewCart_thenShowsDbCart() throws Exception {
        Cart cart = new Cart("user123");

        try (MockedStatic<SecurityContextHolder> security = mockStatic(SecurityContextHolder.class)) {
            SecurityContext context = mock(SecurityContext.class);
            Authentication auth = mock(Authentication.class);

            CurrentUser currentUser = mock(CurrentUser.class);
            User user = mock(User.class);
            when(currentUser.getUser()).thenReturn(user);

            when(auth.isAuthenticated()).thenReturn(true);
            when(auth.getPrincipal()).thenReturn(currentUser);
            when(context.getAuthentication()).thenReturn(auth);
            security.when(SecurityContextHolder::getContext).thenReturn(context);

            when(cartService.getDBCart()).thenReturn(cart);
            when(cartService.calculatePricesForItems(any())).thenReturn(List.of(BigDecimal.TEN));

            mockMvc.perform(get("/cart"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("cart/cart-list"))
                    .andExpect(model().attributeExists("cart"))
                    .andExpect(model().attributeExists("itemPrices"))
                    .andExpect(model().attributeExists("cartTotalPrice"));
        }
    }

    @Test
    void givenAnonymousUser_whenViewCart_thenShowsSessionCart() throws Exception {
        try (MockedStatic<SecurityContextHolder> security = mockStatic(SecurityContextHolder.class)) {
            SecurityContext context = mock(SecurityContext.class);
            Authentication auth = mock(AnonymousAuthenticationToken.class);

            when(auth.isAuthenticated()).thenReturn(true);

            context.setAuthentication(auth);
            when(context.getAuthentication()).thenReturn(auth);
            security.when(SecurityContextHolder::getContext).thenReturn(context);

            when(cartService.getSessionItems()).thenReturn(List.of());
            when(cartService.mapSessionItemsToSessionItemDto(any())).thenReturn(List.of());

            mockMvc.perform(get("/cart"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("cart/cart-list"))
                    .andExpect(model().attributeExists("sessionCart"))
                    .andExpect(model().attributeExists("cartTotalPrice"));
        }
    }


    @Test
    void addItem_redirectsWithMessage() throws Exception {
        mockMvc.perform(post("/cart/add")
                        .param("pizzaId", "15")
                        .param("size", "LARGE")
                        .param("quantity", "3"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(flash().attribute("message", "In den Warenkorb hinzugefügt!"));

        verify(cartService).addItem(argThat(dto ->
                dto.pizzaId() == 15L && dto.pizzaSize() == PizzaSize.LARGE && dto.quantity() == 3));
    }

    @Test
    void deleteItem_works() throws Exception {
        mockMvc.perform(post("/cart/delete/7"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cart"));
        verify(cartService).deleteItem(7L);
    }

    @Test
    void deleteSessionItem_works() throws Exception {
        mockMvc.perform(post("/cart/delete/session/4"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cart"));
        verify(cartService).deleteSessionItem(4L);
    }

    @Test
    void updateItem_works() throws Exception {
        mockMvc.perform(post("/cart/update/9")
                        .param("quantity", "2")
                        .param("size", "SMALL"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cart"));
        verify(cartService).updateItem(eq(9L), any());
    }
}