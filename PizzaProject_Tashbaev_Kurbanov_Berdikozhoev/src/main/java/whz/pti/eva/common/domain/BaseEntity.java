package whz.pti.eva.common.domain;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;

import java.io.Serializable;
import java.util.Objects;

/**
 * Basisklasse für alle Entitäten im Projekt.
 * Stellt eine automatisch generierte ID sowie equals- und hashCode-Methoden bereit.
 *
 * Kann von anderen Entitäten geerbt werden, um Standardfelder zu nutzen.
 */
@MappedSuperclass
public abstract class BaseEntity<PK extends Serializable> {

    /**
     * Eindeutige ID der Entität, automatisch vom Datenbanksystem generiert.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private PK id;

    /**
     * Gibt die ID der Entität zurück.
     *
     * @return ID der Entität
     */
    public PK getId() {
        return id;
    }

    /**
     * Berechnet den Hash-Code basierend auf der ID.
     *
     * @return Hash-Code der Entität
     */
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    /**
     * Vergleicht diese Entität mit einem anderen Objekt auf Gleichheit.
     * Zwei Entitäten gelten als gleich, wenn ihre IDs identisch sind.
     *
     * @param obj das zu vergleichende Objekt
     * @return true, wenn die Objekte gleich sind, sonst false
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if(obj == null || getClass() != obj.getClass()) return false;
        BaseEntity<?> other = (BaseEntity<?>) obj;
        return this.getId() != null && this.getId().equals(other.getId());
    }

    /**
     * Setzt die ID der Entität.
     *
     * HINWEIS: Diese Methode ist primär für Testzwecke oder spezielle Initialisierungen vorgesehen,
     * da die ID im Normalfall automatisch durch die Datenbank generiert wird (GenerationType.IDENTITY).
     *
     * @param id Der zu setzende Wert für den Primärschlüssel.
     */
    public void setIdForTest(PK id) {
        this.id = id;
    }
}
