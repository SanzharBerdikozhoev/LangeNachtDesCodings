package whz.pti.eva.favorite.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import whz.pti.eva.favorite.domain.FavoritePizzaRepository;
import whz.pti.eva.menu.domain.Pizza;
import whz.pti.eva.menu.domain.PizzaRepository;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FavoritePizzaServiceTest {
    @Mock
    private FavoritePizzaRepository favoriteRepository;
    @Mock
    private PizzaRepository pizzaRepository;
    @InjectMocks
    private FavoritePizzaService favoriteService;

    @Test
    void addSavesFavoriteOnce() {
        Pizza pizza = new Pizza("Margherita", BigDecimal.TEN, BigDecimal.TEN, BigDecimal.TEN);
        when(pizzaRepository.findById(7L)).thenReturn(Optional.of(pizza));

        favoriteService.add("alice", 7L);

        verify(favoriteRepository).save(argThat(favorite ->
                favorite.getUserId().equals("alice") && favorite.getPizza() == pizza));
    }

    @Test
    void addDoesNotCreateDuplicateFavorite() {
        when(favoriteRepository.existsByUserIdAndPizzaId("alice", 7L)).thenReturn(true);

        favoriteService.add("alice", 7L);

        verifyNoInteractions(pizzaRepository);
        verify(favoriteRepository, never()).save(any());
    }

    @Test
    void addRejectsUnknownPizza() {
        when(pizzaRepository.findById(7L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> favoriteService.add("alice", 7L));
        verify(favoriteRepository, never()).save(any());
    }
}
