package whz.pti.eva.favorite.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import whz.pti.eva.common.domain.BaseEntity;
import whz.pti.eva.menu.domain.Pizza;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "pizza_id"}))
public class FavoritePizza extends BaseEntity<Long> {
    @Column(name = "user_id", nullable = false)
    private String userId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "pizza_id", nullable = false)
    private Pizza pizza;

    protected FavoritePizza() {}

    public FavoritePizza(String userId, Pizza pizza) {
        this.userId = userId;
        this.pizza = pizza;
    }

    public String getUserId() {
        return userId;
    }

    public Pizza getPizza() {
        return pizza;
    }
}
