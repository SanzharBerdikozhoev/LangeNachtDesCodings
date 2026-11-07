package whz.pti.eva.favorite.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import whz.pti.eva.common.exceptions.PizzaNotFoundException;
import whz.pti.eva.favorite.domain.FavoritePizza;
import whz.pti.eva.favorite.domain.FavoritePizzaRepository;
import whz.pti.eva.menu.domain.PizzaRepository;

import java.util.List;

@Service
public class FavoritePizzaService {
    private final FavoritePizzaRepository favoriteRepository;
    private final PizzaRepository pizzaRepository;

    public FavoritePizzaService(FavoritePizzaRepository favoriteRepository, PizzaRepository pizzaRepository) {
        this.favoriteRepository = favoriteRepository;
        this.pizzaRepository = pizzaRepository;
    }

    public List<FavoritePizza> getFavorites(String userId) {
        return favoriteRepository.findAllByUserId(userId);
    }

    @Transactional
    public void add(String userId, Long pizzaId) {
        if (favoriteRepository.existsByUserIdAndPizzaId(userId, pizzaId)) return;
        var pizza = pizzaRepository.findById(pizzaId)
                .orElseThrow(() -> new PizzaNotFoundException(pizzaId));
        favoriteRepository.save(new FavoritePizza(userId, pizza));
    }

    @Transactional
    public void remove(String userId, Long pizzaId) {
        favoriteRepository.deleteByUserIdAndPizzaId(userId, pizzaId);
    }
}
