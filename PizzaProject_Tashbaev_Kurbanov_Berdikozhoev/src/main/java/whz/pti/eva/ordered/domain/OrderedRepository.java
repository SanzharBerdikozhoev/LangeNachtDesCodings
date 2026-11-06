package whz.pti.eva.ordered.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repository-Schnittstelle für Ordered-Entitäten.
 * Bietet CRUD-Operationen (Erstellen, Lesen, Aktualisieren, Löschen) und JPA-Funktionalitäten.
 * Zusätzlich kann man alle Bestellungen eines bestimmten Benutzers abrufen.
 */
public interface OrderedRepository extends JpaRepository<Ordered, Long> {
    /**
     * Findet alle Bestellungen, die von einem bestimmten Benutzer aufgegeben wurden.
     *
     * @param userId die ID des Benutzers
     * @return Liste der Bestellungen des Benutzers
     */
    List<Ordered> findAllByUserId(String userId);
}
