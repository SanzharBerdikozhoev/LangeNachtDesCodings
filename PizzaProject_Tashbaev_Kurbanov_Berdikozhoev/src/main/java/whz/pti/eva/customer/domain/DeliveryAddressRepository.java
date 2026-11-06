package whz.pti.eva.customer.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository-Interface für die DeliveryAddress-Entität.
 * <p>
 * Erweitert JpaRepository, um CRUD-Operationen (Create, Read, Update, Delete) bereitzustellen.
 */
@Repository
public interface DeliveryAddressRepository extends JpaRepository<DeliveryAddress, Long> {
    /**
     * Sucht nach einer existierenden Lieferadresse anhand aller Adressfelder.
     * <p>
     * Die Suche ignoriert die Groß- und Kleinschreibung (AllIgnoreCase).
     *
     * @param street      Straßenname.
     * @param houseNumber Hausnummer.
     * @param town        Stadt/Ort.
     * @param postalCode  Postleitzahl.
     * @return Ein Optional, das die gefundene Adresse enthält, falls vorhanden.
     */
    Optional<DeliveryAddress> findByStreetAndHouseNumberAndTownAndPostalCodeAllIgnoreCase(
            String street, String houseNumber, String town, String postalCode);
}
