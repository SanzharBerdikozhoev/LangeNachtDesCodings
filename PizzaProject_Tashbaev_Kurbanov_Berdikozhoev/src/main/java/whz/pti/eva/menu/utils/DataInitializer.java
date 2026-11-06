package whz.pti.eva.menu.utils;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import whz.pti.eva.menu.domain.Pizza;
import whz.pti.eva.menu.domain.PizzaRepository;

import java.math.BigDecimal;

/**
 * Initialisiert die Datenbank mit Standard-Pizzen bei Anwendungsstart, falls keine vorhanden sind.
 * Implementiert CommandLineRunner für einmalige Ausführung beim Booten.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private final PizzaRepository pizzaRepository;

    /**
     * Konstruktor, der das PizzaRepository injiziert.
     * @param pizzaRepository Repository für Pizza-Persistenz
     */
    public DataInitializer(PizzaRepository pizzaRepository) {
        this.pizzaRepository = pizzaRepository;
    }

    /**
     * Führt die Initialisierung aus: Fügt Standard-Pizzen hinzu, wenn die DB leer ist.
     * @param args Kommandozeilenargumente (nicht verwendet)
     */
    @Override
    public void run(String... args) {
        if (pizzaRepository.count() == 0) {
            pizzaRepository.save(new Pizza("Margherita",
                    new BigDecimal("7.99"),
                    new BigDecimal("5.99"),
                    new BigDecimal("3.99")));

            pizzaRepository.save(new Pizza("Tonno",
                    new BigDecimal("8.99"),
                    new BigDecimal("6.99"),
                    new BigDecimal("4.99")));

            pizzaRepository.save(new Pizza("Veggie",
                    new BigDecimal("9.99"),
                    new BigDecimal("7.99"),
                    new BigDecimal("5.99")));

            pizzaRepository.save(new Pizza("Funghi",
                    new BigDecimal("8.49"),
                    new BigDecimal("6.49"),
                    new BigDecimal("4.49")));

            pizzaRepository.save(new Pizza("Pepperoni",
                    new BigDecimal("10.99"),
                    new BigDecimal("8.99"),
                    new BigDecimal("6.99")));

            pizzaRepository.save(new Pizza("Hawaiian",
                    new BigDecimal("11.99"),
                    new BigDecimal("9.99"),
                    new BigDecimal("7.99")));

            pizzaRepository.save(new Pizza("BBQ Chicken",
                    new BigDecimal("12.99"),
                    new BigDecimal("10.99"),
                    new BigDecimal("8.99")));
            System.out.println("Default pizzas added to database!");
        } else {
            System.out.println("ℹPizzas already exist, skipping initialization.");
        }
    }
}
