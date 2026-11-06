package whz.pti.eva.ordered.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import whz.pti.eva.cart.domain.Cart;
import whz.pti.eva.cart.domain.CartRepository;
import whz.pti.eva.cart.domain.Item;
import whz.pti.eva.cart.service.CartService;
import whz.pti.eva.menu.domain.Pizza;
import whz.pti.eva.menu.domain.PizzaSize;
import whz.pti.eva.ordered.domain.Ordered;
import whz.pti.eva.ordered.domain.OrderedItem;
import whz.pti.eva.ordered.domain.OrderedItemRepository;
import whz.pti.eva.ordered.domain.OrderedRepository;
import whz.pti.eva.payment.service.PaymentService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class OrderedServiceImplTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartService cartService;

    @Mock
    private OrderedRepository orderedRepository;

    @Mock
    private OrderedItemRepository orderedItemRepository;

    @Mock
    private PaymentService paymentService;


    @InjectMocks
    private OrderedServiceImpl orderedService;

    private String testUserId = "42";
    private Cart cart;
    private List<Pizza> pizzas;
    private List<Item> items;
    private List<Ordered> preloadedOrders;

    @BeforeEach
    public void setup() {
        preloadedOrders = List.of(
                new Ordered(testUserId),
                new Ordered(testUserId),
                new Ordered(testUserId)
        );

        pizzas = List.of(
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

        items = List.of(
                new Item(10, PizzaSize.SMALL, pizzas.get(0), cart),
                new Item(3, PizzaSize.SMALL, pizzas.get(0), cart),
                new Item(4, PizzaSize.MEDIUM, pizzas.get(1), cart),
                new Item(1, PizzaSize.SMALL, pizzas.get(2), cart),
                new Item(3, PizzaSize.LARGE, pizzas.get(3), cart)
        );

        items.get(0).setIdForTest(1L);
        items.get(1).setIdForTest(2L);
        items.get(2).setIdForTest(3L);
        items.get(3).setIdForTest(4L);
        items.get(4).setIdForTest(5L);

        cart = new Cart(testUserId);
        cart.setItems(items);
    }

    @Test
    public void testAddOrdered() {
        Long deliveryAddressId = 1L;
        List<Long> selectedItemIds = List.of(1L, 2L, 3L, 4L, 5L);

        when(cartRepository.findByUserId(testUserId)).thenReturn(Optional.of(cart));
        doNothing().when(paymentService).doActionPay(eq(testUserId), anyDouble());

        when(orderedRepository.save(any(Ordered.class))).thenAnswer(i -> {
            Ordered o = i.getArgument(0);
            o.setIdForTest(100L);
            return o;
        });

        Long orderedId = orderedService.addOrdered(testUserId, selectedItemIds, deliveryAddressId);

        ArgumentCaptor<Ordered> orderedCaptor = ArgumentCaptor.forClass(Ordered.class);
        verify(orderedRepository, times(1)).save(orderedCaptor.capture());
        Ordered savedOrdered = orderedCaptor.getValue();

        verify(cartService, times(5)).deleteItem(anyLong());

        assertNotNull(savedOrdered.getItems(), "Items müssen nicht null sein");
        assertEquals(5, savedOrdered.getItems().size(), "Es muss 5 Items sein");
        assertEquals(5, savedOrdered.getNumberOfItems(), "NumberOfItems muss Artikelsmenge gleich sein");
        assertTrue(savedOrdered.getTotalPrice().compareTo(BigDecimal.ZERO) > 0, "TotalPrice muss größer als 0 sein");
        assertEquals(deliveryAddressId, savedOrdered.getDeliveryAddressId(), "DeliveryAddressId muss übereinstimmen");
        assertEquals(testUserId, savedOrdered.getUserId(), "UserId muss übereinstimmen");
        assertEquals(100L, orderedId, "Returned ID muss übereinstimmen");

        for (OrderedItem item : savedOrdered.getItems()) {
            assertNotNull(item.getName(), "Pizzas Name muss nicht null sein");
            assertTrue(item.getQuantity() > 0, "Pizzas Menge muss größer als 0 sein");
            assertNotNull(item.getPrice(), "Pizzas Preis muss nicht null sein");
        }
    }


    @Test
    public void testGetAllOrdered() {
        when(orderedRepository.findAllByUserId(testUserId)).thenReturn(preloadedOrders);

        List<Ordered> orders = orderedService.getAllOrdered(testUserId);

        assertEquals(preloadedOrders.size(), orders.size());
        for (int i = 0; i < preloadedOrders.size(); i++) {
            assertEquals(preloadedOrders.get(i), orders.get(i));
        }

        verify(orderedRepository, times(1)).findAllByUserId(testUserId);
    }


    @Test
    public void testDeleteOrdered() {
        orderedService.deleteOrdered(preloadedOrders.get(1));
        verify(orderedRepository, times(1)).delete(preloadedOrders.get(1));
    }
}