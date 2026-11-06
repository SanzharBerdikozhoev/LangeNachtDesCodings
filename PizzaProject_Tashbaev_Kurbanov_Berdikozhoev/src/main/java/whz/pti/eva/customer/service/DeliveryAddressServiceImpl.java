package whz.pti.eva.customer.service;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import whz.pti.eva.customer.domain.Customer;
import whz.pti.eva.customer.domain.CustomerRepository;
import whz.pti.eva.customer.domain.DeliveryAddress;
import whz.pti.eva.customer.domain.DeliveryAddressRepository;

import java.util.Objects;
import java.util.Optional;


/**
 * Service-Implementierung zur Verwaltung von Lieferadressen.
 *
 * Stellt Methoden zum Speichern, Aktualisieren und Verknüpfen von Adressen
 * mit dem Kundenprofil bereit und verhindert Duplikate.
 */
@Service
public class DeliveryAddressServiceImpl implements DeliveryAddressService {

    private final DeliveryAddressRepository deliveryAddressRepository;
    private final CustomerRepository customerRepository;

    /**
     * Konstruktor zur Injektion der benötigten Repositories.
     *
     * @param deliveryAddressRepository Repository für Adressen.
     * @param customerRepository Repository für Kunden.
     */
    public DeliveryAddressServiceImpl(DeliveryAddressRepository deliveryAddressRepository, CustomerRepository customerRepository) {
        this.deliveryAddressRepository = deliveryAddressRepository;
        this.customerRepository = customerRepository;
    }


    /**
     * Speichert eine neue Lieferadresse oder verknüpft eine bereits existierende Adresse mit dem Kunden.
     *
     * Prüft auf Duplikate und vermeidet redundante Adress-Einträge.
     *
     * @param deliveryAddress Die Adresse, die gespeichert oder verknüpft werden soll.
     * @param customerId Die ID des Kunden, dem die Adresse zugeordnet wird.
     * @return Die gespeicherte oder verknüpfte DeliveryAddress-Entität.
     * @throws IllegalArgumentException wenn der Kunde nicht gefunden wird.
     */
    @Override
    @Transactional
    public DeliveryAddress save(DeliveryAddress deliveryAddress, Long customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer wurde nicht gefunden"));

        DeliveryAddress existing = findExisting(deliveryAddress).orElse(null);
        DeliveryAddress toLink = Objects.requireNonNullElseGet(existing, () -> deliveryAddressRepository.save(deliveryAddress));

        if (!toLink.getCustomers().contains(customer)) {
            customer.addDeliveryAddress(toLink);
            customerRepository.save(customer);
        }

        return toLink;
    }

    /**
     * Aktualisiert die Details einer existierenden Lieferadresse.
     *
     * Es wird eine RuntimeException ausgelöst, wenn die Adresse nicht gefunden wird.
     *
     * @param id Die ID der zu aktualisierenden Adresse.
     * @throws RuntimeException wenn die Adresse nicht gefunden wird.
     */
    @Override
    @Transactional
    public void update(Long id, DeliveryAddress updatedAddress, Long customerId) {
        DeliveryAddress existingAddress = deliveryAddressRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Delivery address wurde nicht gefunden"));

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer wurde nicht gefunden"));

        if (!existingAddress.getCustomers().contains(customer)) {
            throw new IllegalArgumentException("Das ist nicht ihre Adresse");
        }

        Optional<DeliveryAddress> duplicate = findExisting(updatedAddress);

        DeliveryAddress addressToUse;

        if (duplicate.isPresent()) {
            addressToUse = duplicate.get();
        } else {
            addressToUse = new DeliveryAddress(
                    updatedAddress.getStreet(),
                    updatedAddress.getHouseNumber(),
                    updatedAddress.getTown(),
                    updatedAddress.getPostalCode()
            );
            deliveryAddressRepository.save(addressToUse);
        }

        customer.removeDeliveryAddress(existingAddress);
        customer.addDeliveryAddress(addressToUse);
        customerRepository.save(customer);

    }



    /**
     * Entfernt die Verknüpfung einer Adresse vom Kundenprofil.
     *
     * Hinweis: Diese Logik dient der Aufhebung der Many-to-Many-Beziehung.
     * Die Adresse selbst wird hierbei nicht aus der Datenbank gelöscht.
     *
     * @param addressId Die ID der Adresse, die entfernt werden soll.
     * @param customerId Die ID des Kunden.
     * @throws IllegalArgumentException wenn Kunde, Adresse oder Verknüpfung nicht gefunden werden.
     */
    @Override
    @Transactional
    public void delete(Long addressId, Long customerId) {
        DeliveryAddress address = deliveryAddressRepository.findById(addressId)
                .orElseThrow(() -> new IllegalArgumentException("Adresse wurde nicht gefunden"));

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer wurde nicht gefunden"));

        if (customer.getDeliveryAddresses().size() <= 1) {
            throw new IllegalStateException("Die letzte Adresse kann nicht gelöscht werden");
        }

        if (!address.getCustomers().contains(customer)) {
            throw new IllegalArgumentException("Das ist nicht ihre Adresse");
        }

        customer.removeDeliveryAddress(address);
        customerRepository.save(customer);
    }

    /**
     * Sucht nach einer exakt übereinstimmenden Adresse in der Datenbank.
     *
     * Die Suche basiert auf Straße, Hausnummer, Ort und Postleitzahl und ignoriert Leerzeichen.
     *
     * @param address Die Adresse mit den Suchkriterien.
     * @return Ein Optional mit der gefundenen Adresse, falls Duplikat gefunden wurde.
     */
    private Optional<DeliveryAddress> findExisting(DeliveryAddress address) {
        return deliveryAddressRepository.findByStreetAndHouseNumberAndTownAndPostalCodeAllIgnoreCase(
                address.getStreet().trim(),
                address.getHouseNumber().trim(),
                address.getTown().trim(),
                address.getPostalCode().trim()
        );
    }
}
