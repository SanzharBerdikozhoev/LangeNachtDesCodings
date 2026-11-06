package whz.pti.eva.menu.domain;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * JPA-Repository-Schnittstelle für die Persistenz von Pizza-Entitäten.
 * Ermöglicht Standard-CRUD-Operationen für Pizzen in der Datenbank.
 */
public interface PizzaRepository extends JpaRepository<Pizza, Long> {
    java.util.List<Pizza> findByNameContainingIgnoreCase(String name);
}
