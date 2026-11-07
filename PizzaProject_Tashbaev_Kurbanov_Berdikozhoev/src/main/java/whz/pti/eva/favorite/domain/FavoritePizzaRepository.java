package whz.pti.eva.favorite.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FavoritePizzaRepository extends JpaRepository<FavoritePizza, Long> {
    List<FavoritePizza> findAllByUserId(String userId);
    boolean existsByUserIdAndPizzaId(String userId, Long pizzaId);
    void deleteByUserIdAndPizzaId(String userId, Long pizzaId);
}
