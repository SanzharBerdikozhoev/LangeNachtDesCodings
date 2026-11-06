package whz.pti.eva.cart.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import whz.pti.eva.cart.domain.*;
import whz.pti.eva.cart.domain.session.SessionCart;
import whz.pti.eva.cart.domain.session.SessionCartItem;
import whz.pti.eva.common.exceptions.ItemNotFoundException;
import whz.pti.eva.common.exceptions.PizzaNotFoundException;
import whz.pti.eva.menu.domain.Pizza;
import whz.pti.eva.menu.domain.PizzaRepository;
import whz.pti.eva.menu.domain.PizzaSize;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
class CartServiceImplTest {

    @Mock private CartRepository cartRepository;
    @Mock private ItemRepository itemRepository;
    @Mock private PizzaRepository pizzaRepository;
    @Mock private SessionCart sessionCart;

    private CartServiceImpl cartService;
    private CartServiceImpl cartServiceSpy;

    private final String testUserId = "user123";

    @BeforeEach
    void setup() {
        cartService = new CartServiceImpl(cartRepository, itemRepository, pizzaRepository, sessionCart);
        cartServiceSpy = spy(cartService);
    }

    @Test
    void givenLoggedInUser_whenAddItem_thenAddsToDbCart() {
        doReturn(true).when(cartServiceSpy).isLoggedIn();
        doReturn(testUserId).when(cartServiceSpy).getCurrentUserId();

        Cart cart = new Cart(testUserId);
        when(cartRepository.findByUserIdWithItems(testUserId)).thenReturn(Optional.of(cart));

        Pizza pizza = new Pizza();
        when(pizzaRepository.findById(1L)).thenReturn(Optional.of(pizza));

        cartServiceSpy.addItem(new ItemRequestDto(1L, PizzaSize.LARGE, 2));

        assertEquals(1, cart.getItems().size());
        Item item = cart.getItems().get(0);
        assertEquals(pizza, item.getPizza());
        assertEquals(PizzaSize.LARGE, item.getSize());
        assertEquals(2, item.getQuantity());

        verify(itemRepository).save(any(Item.class));
        verify(cartRepository).save(cart);
    }

    @Test
    void givenAnonymousUser_whenAddItem_thenAddsToSessionCart() {
        doReturn(false).when(cartServiceSpy).isLoggedIn();

        cartServiceSpy.addItem(new ItemRequestDto(5L, PizzaSize.SMALL, 3));

        verify(sessionCart).add(5L, PizzaSize.SMALL, 3);
        verify(cartRepository, never()).findByUserIdWithItems(any());
    }

    @Test
    void givenPizzaNotFound_whenAddItem_thenThrowsException() {
        doReturn(true).when(cartServiceSpy).isLoggedIn();
        doReturn(testUserId).when(cartServiceSpy).getCurrentUserId();

        when(cartRepository.findByUserIdWithItems(testUserId)).thenReturn(Optional.of(new Cart(testUserId)));
        when(pizzaRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(PizzaNotFoundException.class,
                () -> cartServiceSpy.addItem(new ItemRequestDto(999L, PizzaSize.MEDIUM, 1)));
    }

    @Test
    void givenExistingItem_whenUpdateItem_thenUpdatesSuccessfully() {
        Item item = new Item(1, PizzaSize.SMALL, new Pizza(), new Cart(testUserId));
        when(itemRepository.findById(10L)).thenReturn(Optional.of(item));

        cartServiceSpy.updateItem(10L, new ItemRequestDto(null, PizzaSize.LARGE, 5));

        assertEquals(5, item.getQuantity());
        assertEquals(PizzaSize.LARGE, item.getSize());
        verify(itemRepository).save(item);
        verify(cartRepository).save(item.getCart());
    }

    @Test
    void givenItemNotFound_whenUpdateItem_thenThrowsException() {
        when(itemRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ItemNotFoundException.class,
                () -> cartServiceSpy.updateItem(999L, new ItemRequestDto(null, PizzaSize.MEDIUM, 1)));
    }

    @Test
    void givenSessionItem_whenUpdateSessionItem_thenDelegatesToSessionCart() {
        cartServiceSpy.updateSessionItem(2L, new ItemRequestDto(null, PizzaSize.MEDIUM, 4));
        verify(sessionCart).update(2L, PizzaSize.MEDIUM, 4);
    }

    @Test
    void givenExistingItem_whenDeleteItem_thenRemovesSuccessfully() {
        Item item = new Item();
        Cart cart = new Cart(testUserId);
        cart.addItem(item);
        item.setCart(cart);

        when(itemRepository.findById(7L)).thenReturn(Optional.of(item));

        cartServiceSpy.deleteItem(7L);

        assertTrue(cart.getItems().isEmpty());
        verify(cartRepository).save(cart);
    }

    @Test
    void givenItemNotFound_whenDeleteItem_thenThrowsException() {
        when(itemRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ItemNotFoundException.class, () -> cartServiceSpy.deleteItem(999L));
    }

    @Test
    void givenSessionItem_whenDeleteSessionItem_thenDelegatesToSessionCart() {
        cartServiceSpy.deleteSessionItem(5L);
        verify(sessionCart).removeByIndex(5L);
    }

    @Test
    void givenSessionItems_whenMergeIntoDbCart_thenItemsTransferred() {
        Cart dbCart = new Cart(testUserId);
        when(cartRepository.findByUserIdWithItems(testUserId)).thenReturn(Optional.of(dbCart));

        SessionCartItem sessionItem = new SessionCartItem(1L, 2, PizzaSize.SMALL);
        when(sessionCart.getItems()).thenReturn(List.of(sessionItem));

        Pizza pizza = new Pizza();
        when(pizzaRepository.findById(1L)).thenReturn(Optional.of(pizza));

        cartServiceSpy.mergeSessionCartIntoDb(testUserId);

        assertEquals(1, dbCart.getItems().size());
        verify(cartRepository).save(dbCart);
        verify(sessionCart).clear();
    }
}