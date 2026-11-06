package whz.pti.eva.menu.service;

import org.springframework.stereotype.Service;
import whz.pti.eva.menu.domain.Pizza;
import whz.pti.eva.menu.domain.PizzaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Implementierung des PizzaService für die Verwaltung von Pizzen in der Pizza-Anwendung.
 * Bietet CRUD-Operationen mit Validierungen für Löschung und Aktualisierung.
 */
@Service
public class PizzaServiceImpl implements PizzaService {

    private final PizzaRepository pizzaRepository;

    /**
     * Konstruktor, der das PizzaRepository injiziert.
     * @param pizzaRepository Repository für Pizza-Persistenz
     */
    public PizzaServiceImpl(PizzaRepository pizzaRepository) {
        this.pizzaRepository = pizzaRepository;
    }

    /**
     * Gibt eine Liste aller Pizzen zurück.
     * @return Liste aller Pizzen
     */
    @Override
    public List<Pizza> listAll() {
        return pizzaRepository.findAll();
    }

    @Override
    public List<Pizza> searchByName(String name) {
        if (name == null || name.isBlank()) {
            return listAll();
        }
        return pizzaRepository.findByNameContainingIgnoreCase(name.trim());
    }

    /**
     * Ruft eine Pizza anhand ihrer ID ab.
     * @param id ID der Pizza
     * @return Optional mit der Pizza
     */
    @Override
    public Optional<Pizza> getById(Long id) {
        return pizzaRepository.findById(id);
    }

    /**
     * Löscht eine Pizza anhand ihrer ID, wenn sie nicht in Warenkörben verwendet wird.
     * @param id ID der Pizza
     * @throws IllegalArgumentException bei ungültiger ID
     * @throws IllegalStateException wenn Pizza in Warenkörben vorhanden
     */
    @Override
    public void deleteById(Long id) {
        Pizza pizza = pizzaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Ungültige Pizza-ID"));

        if (!pizza.getItems().isEmpty()) {
            throw new IllegalStateException(
                    "Diese Pizza ist noch in einigen Warenkörben. Löschen nicht möglich."
            );
        }

        pizzaRepository.deleteById(id);
    }

    /**
     * Fügt eine neue Pizza zur Datenbank hinzu.
     * @param pizza Neue Pizza
     */
    @Override
    public void addPizza(Pizza pizza) {
        pizzaRepository.save(pizza);
    }

    /**
     * Aktualisiert Name und Preise einer Pizza anhand ihrer ID.
     * @param id ID der Pizza
     * @param pizza Aktualisierte Daten
     */
    @Override
    public void updateById(Long id, Pizza pizza) {
        Optional<Pizza> pizzaResult = pizzaRepository.findById(id);
        if(pizzaResult.isPresent()) {
            Pizza pizzaToUpdate = pizzaResult.get();
            pizzaToUpdate.setName(pizza.getName());
            pizzaToUpdate.setPriceSmall(pizza.getPriceSmall());
            pizzaToUpdate.setPriceMedium(pizza.getPriceMedium());
            pizzaToUpdate.setPriceLarge(pizza.getPriceLarge());
            pizzaRepository.save(pizzaToUpdate);
        }
    }
}
