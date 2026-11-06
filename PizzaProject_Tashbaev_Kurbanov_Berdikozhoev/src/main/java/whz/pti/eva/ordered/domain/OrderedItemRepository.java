package whz.pti.eva.ordered.domain;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository-Schnittstelle für OrderedItem-Entitäten.
 * Bietet CRUD-Operationen (Erstellen, Lesen, Aktualisieren, Löschen) und JPA-Funktionalitäten.
 */
public interface OrderedItemRepository extends JpaRepository<OrderedItem, Long> {
}
