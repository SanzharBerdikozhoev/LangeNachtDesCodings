package whz.pti.eva.menu.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import whz.pti.eva.menu.domain.Pizza;
import whz.pti.eva.menu.domain.PizzaRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
class PizzaServiceImplTest {

  @Mock
  private PizzaRepository pizzaRepository;

  @InjectMocks
  private PizzaServiceImpl pizzaService;

  private Pizza samplePizza;

  @BeforeEach
  void setUp() {
    samplePizza = new Pizza("Margherita", BigDecimal.TEN, BigDecimal.TEN, BigDecimal.TEN);
  }

  @Test
  @DisplayName("listAll should return all pizzas")
  void listAll_ShouldReturnAllPizzas() {
    List<Pizza> expectedPizzas = List.of(samplePizza);
    when(pizzaRepository.findAll()).thenReturn(expectedPizzas);

    List<Pizza> result = pizzaService.listAll();

    assertEquals(expectedPizzas, result);
    verify(pizzaRepository, times(1)).findAll();
  }

  @Test
  @DisplayName("getById should return pizza by id")
  void getById_ShouldReturnPizza() {
    when(pizzaRepository.findById(1L)).thenReturn(Optional.of(samplePizza));

    Optional<Pizza> result = pizzaService.getById(1L);

    assertTrue(result.isPresent());
    assertEquals(samplePizza, result.get());
    verify(pizzaRepository, times(1)).findById(1L);
  }

  @Test
  @DisplayName("getById should return empty optional if not found")
  void getById_ShouldReturnEmptyOptionalIfNotFound() {
    when(pizzaRepository.findById(999L)).thenReturn(Optional.empty());

    Optional<Pizza> result = pizzaService.getById(999L);

    assertFalse(result.isPresent());
    verify(pizzaRepository, times(1)).findById(999L);
  }

  @Test
  @DisplayName("deleteById should delete pizza if no items")
  void deleteById_ShouldDeletePizzaIfNoItems() {
    Pizza mockPizza = mock(Pizza.class);
    when(mockPizza.getItems()).thenReturn(List.of());
    when(pizzaRepository.findById(1L)).thenReturn(Optional.of(mockPizza));

    pizzaService.deleteById(1L);

    verify(pizzaRepository, times(1)).findById(1L);
    verify(mockPizza, times(1)).getItems();
    verify(pizzaRepository, times(1)).deleteById(1L);
  }

  @Test
  @DisplayName("deleteById should throw IllegalArgumentException if not found")
  void deleteById_ShouldThrowIllegalArgumentExceptionIfNotFound() {
    when(pizzaRepository.findById(999L)).thenReturn(Optional.empty());

    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
        () -> pizzaService.deleteById(999L));
    assertEquals("Ungültige Pizza-ID", exception.getMessage());
    verify(pizzaRepository, times(1)).findById(999L);
    verify(pizzaRepository, never()).deleteById(anyLong());
  }

  @Test
  @DisplayName("addPizza should save new pizza")
  void addPizza_ShouldSavePizza() {
    pizzaService.addPizza(samplePizza);

    verify(pizzaRepository, times(1)).save(samplePizza);
  }

    @Test
    @DisplayName("updateById should save updated pizza")
    void updateById_ShouldSaveUpdatedPizza() {
        Pizza existingPizza = new Pizza("Margherita", BigDecimal.TEN, BigDecimal.TEN, BigDecimal.TEN);
        when(pizzaRepository.findById(1L)).thenReturn(Optional.of(existingPizza));

        Pizza updatedPizza = new Pizza("Updated Margherita", BigDecimal.valueOf(12),
                BigDecimal.valueOf(11), BigDecimal.valueOf(10));

        pizzaService.updateById(1L, updatedPizza);

        verify(pizzaRepository, times(1)).save(existingPizza);
    }

}
