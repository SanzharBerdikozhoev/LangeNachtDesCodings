package whz.pti.eva.ordered.service;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import whz.pti.eva.common.exceptions.PaymentFailedException;
import whz.pti.eva.payment.service.PaymentService;
import whz.pti.eva.cart.domain.Cart;
import whz.pti.eva.cart.domain.CartRepository;
import whz.pti.eva.cart.domain.Item;
import whz.pti.eva.cart.service.CartService;
import whz.pti.eva.common.exceptions.CartNotFoundException;
import whz.pti.eva.common.exceptions.ItemNotFoundException;
import whz.pti.eva.customer.domain.DeliveryAddress;
import whz.pti.eva.customer.domain.DeliveryAddressRepository;
import whz.pti.eva.ordered.domain.Ordered;
import whz.pti.eva.ordered.domain.OrderedItem;
import whz.pti.eva.ordered.domain.OrderedRepository;

import java.math.BigDecimal;
import java.util.List;

/**
 * Service-Implementierung für die Verwaltung von Bestellungen.
 * Beinhaltet Funktionen zum Erstellen, Abrufen, Löschen und Speichern von Bestellungen,
 * einschließlich Unterstützung für DB- und Session-Items.
 */
@Service
public class OrderedServiceImpl implements OrderedService {

    private final OrderedRepository orderedRepository;
    private final CartRepository cartRepository;
    private final CartService cartService;
    private final DeliveryAddressRepository deliveryAddressRepository;
    private final PaymentService paymentService;

    /**
     * Konstruktor zur Injektion der benötigten Repositories.
     *
     * @param orderedRepository         Repository für Bestellungen
     * @param cartRepository            Repository für Warenkörbe
     * @param cartService               Service für Warenkorb-Operationen
     * @param deliveryAddressRepository Repository für Adressen
     */
    public OrderedServiceImpl(OrderedRepository orderedRepository,
                              CartRepository cartRepository,
                              CartService cartService,
                              DeliveryAddressRepository deliveryAddressRepository,
                              PaymentService paymentService) {
        this.orderedRepository = orderedRepository;
        this.cartRepository = cartRepository;
        this.cartService = cartService;
        this.deliveryAddressRepository = deliveryAddressRepository;
        this.paymentService = paymentService;
    }

    /**
     * Erstellt eine neue Bestellung basierend auf dem Warenkorb des Benutzers.
     *
     * @param userId  ID des Benutzers
     * @param itemIds Liste der Item-IDs aus dem Warenkorb
     * @return ID der erstellten Bestellung
     * @throws CartNotFoundException wenn der Warenkorb nicht gefunden wird
     * @throws ItemNotFoundException wenn keine Items ausgewählt wurden
     */
    @Override
    @Transactional
    public Long addOrdered(String userId, List<Long> itemIds, Long deliveryAddressId) {

        if (itemIds.isEmpty()) {
            throw new ItemNotFoundException("Es wurden keine Artikel ausgewählt");
        }

        Cart cart = cartRepository.findByUserId(userId).orElseThrow(() -> new CartNotFoundException("Warenkorb wurde nicht gefunden"));

        List<Item> items = cart.getItems().stream()
                .filter(i -> itemIds.contains(i.getId()))
                .toList();

        if (items.isEmpty()) {
            throw new ItemNotFoundException("Keine Artikel wurde gefunden");
        }

        Ordered ordered = new Ordered();

        BigDecimal[] totalPrice = new BigDecimal[]{BigDecimal.ZERO};
        ordered.setItems(items.stream().map(
                item -> {
                    BigDecimal priceForItem = item
                            .getPizza()
                            .getPriceForSize(item.getSize())
                            .multiply(
                                    BigDecimal.valueOf(item.getQuantity()));

                    cartService.deleteItem(item.getId());
                    totalPrice[0] = totalPrice[0].add(priceForItem);

                    return new OrderedItem(
                            item.getPizza().getId(),
                            item.getPizza().getName(),
                            item.getQuantity(),
                            priceForItem,
                            userId,
                            item.getSize());
                }
        ).toList());

        ordered.setUserId(userId);
        ordered.setDeliveryAddressId(deliveryAddressId);
        ordered.setTotalPrice(totalPrice[0]);
        ordered.setNumberOfItems(items.size());

        try {
            paymentService.doActionPay(userId, totalPrice[0].doubleValue());
        } catch (PaymentFailedException e) {
            throw e;
        }

        orderedRepository.save(ordered);

        return ordered.getId();
    }

    /**
     * Gibt alle Bestellungen eines Benutzers zurück.
     *
     * @param userId ID des Benutzers
     * @return Liste aller Bestellungen des Benutzers
     */
    @Override
    public List<Ordered> getAllOrdered(String userId) {
        return orderedRepository.findAllByUserId(userId);
    }

    /**
     * Löscht eine gegebene Bestellung.
     *
     * @param ordered die zu löschende Bestellung
     */
    @Override
    public void deleteOrdered(Ordered ordered) {
        orderedRepository.delete(ordered);
    }

    /**
     * Gibt alle Bestellungen des Benutzers mit der Lieferadresse als DTO zurück.
     *
     * @param userId Nutzer-Name
     * @return Liste der Bestellungen mit Lieferadressen
     */
    @Override
    public List<OrderedWithDeliveryAddressDto> getAllOrderedWithAddress(String userId) {
        List<Ordered> orders = orderedRepository.findAllByUserId(userId);
        return orders.stream()
                .map(o -> new OrderedWithDeliveryAddressDto(
                        o,
                        deliveryAddressRepository.findById(o.getDeliveryAddressId())
                                .map(DeliveryAddress::toString)
                                .orElse("Keine Adresse")
                ))
                .toList();
    }
}