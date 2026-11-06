package whz.pti.eva.ordered.service;

import whz.pti.eva.ordered.domain.Ordered;
import whz.pti.eva.ordered.service.OrderedWithDeliveryAddressDto;

import java.util.List;

/**
 * Service-Interface für Bestellungen.
 * Definiert Operationen zum Abrufen, Erstellen, Speichern und Löschen von Bestellungen.
 */
public interface OrderedService {

    /**
     * Liefert alle Bestellungen eines bestimmten Benutzers.
     *
     * @param userId ID des Benutzers
     * @return Liste aller Bestellungen des Benutzers
     */
    List<Ordered> getAllOrdered(String userId);

    /**
     * Legt eine neue Bestellung für einen eingeloggten Benutzer an.
     *
     * @param userId  ID des Benutzers
     * @param itemIds Liste der DB-Item-IDs, die bestellt werden sollen
     * @return ID der neu erstellten Bestellung
     */
    Long addOrdered(String userId, List<Long> itemIds, Long deliveryAddressId);

    /**
     * Löscht eine gegebene Bestellung.
     *
     * @param ordered Bestellung, die gelöscht werden soll
     */
    void deleteOrdered(Ordered ordered);

    List<OrderedWithDeliveryAddressDto> getAllOrderedWithAddress(String loginName);
}
