package whz.pti.eva.menu.service;

import whz.pti.eva.menu.domain.Pizza;

import java.util.List;
import java.util.Optional;

/**
 * Definiert die Service-Schnittstelle für Operationen mit Pizzen in der Pizza-Anwendung.
 * Ermöglicht das Verwalten des Pizza-Katalogs (Auflisten, Abrufen, Hinzufügen, Aktualisieren, Löschen).
 */
public interface PizzaService {

    /**
     * Gibt eine Liste aller verfügbaren Pizzen zurück.
     * @return Liste aller Pizzen
     */
    List<Pizza> listAll();

    /** Finds pizzas whose name contains the supplied text, ignoring case. */
    List<Pizza> searchByName(String name);

    /**
     * Ruft eine Pizza anhand ihrer ID ab.
     * @param id ID der Pizza
     * @return Optional mit der Pizza, falls gefunden
     */
    Optional<Pizza> getById(Long id);

    /**
     * Löscht eine Pizza anhand ihrer ID.
     * @param id ID der zu löschenden Pizza
     */
    void deleteById(Long id);

    /**
     * Fügt eine neue Pizza zum Katalog hinzu.
     * @param pizza Neue Pizza-Daten
     */
    void addPizza(Pizza pizza);

    /**
     * Aktualisiert eine bestehende Pizza anhand ihrer ID.
     * @param id ID der zu aktualisierenden Pizza
     * @param pizza Aktualisierte Pizza-Daten
     */
    void updateById(Long id, Pizza pizza);
}
