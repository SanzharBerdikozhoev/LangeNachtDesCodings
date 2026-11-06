package whz.pti.eva.customer.service;

import whz.pti.eva.customer.domain.DeliveryAddress;

import java.util.List;

/**
 * Service-Schnittstelle für die Verwaltung der Lieferadressen.
 *
 * Definiert Operationen zum Speichern, Aktualisieren und Löschen von Adressen
 * unter Berücksichtigung der Kundenbeziehung.
 */
public interface DeliveryAddressService {
    /**
     * Speichert eine neue Lieferadresse und ordnet sie einem bestimmten Kunden zu.
     *
     * @param deliveryAddress Die zu speichernde Adresse.
     * @param customerId Die ID des Kunden, dem die Adresse zugeordnet werden soll.
     * @return Die gespeicherte DeliveryAddress-Entität.
     */
    DeliveryAddress save(DeliveryAddress deliveryAddress, Long customerId);


    /**
     * Aktualisiert die Details einer existierenden Lieferadresse.
     *
     * @param id Die ID der zu aktualisierenden Adresse.
     */
    void update(Long id, DeliveryAddress updatedAddress, Long customerId) ;

    /**
     * Löscht eine Lieferadresse.
     *
     * Muss die Geschäftslogik berücksichtigen, dass mindestens eine Adresse
     * beim Kunden bestehen bleiben muss.
     *
     * @param addressId Die ID der zu löschenden Adresse.
     * @param customerId Die ID des Kunden, von dem die Adresse entfernt wird.
     */
    void delete(Long addressId, Long customerId);
}
