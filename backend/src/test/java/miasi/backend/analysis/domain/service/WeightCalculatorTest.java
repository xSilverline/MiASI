package miasi.backend.analysis.domain.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import miasi.backend.analysis.domain.model.core.Resource;
import miasi.backend.analysis.domain.model.modules.Module;
import miasi.backend.common.domain.model.ResourceType;
import org.junit.jupiter.api.Test;

class WeightCalculatorTest {

  @Test
  void shouldCalculateTotalWeight() {
    // Given
    WeightCalculator calculator = new WeightCalculator();
    float startWeight = 50f;
    float amountToAdd = 5f;
    ResourceType type = ResourceType.FOOD;
    Module module = mock(Module.class);

    when(module.getWeight()).thenReturn(startWeight);

    Resource resource = new Resource(type, amountToAdd);

    // When
    float result = calculator.calculateTotalWeight(List.of(module), List.of(resource));

    // Then
    assertEquals(startWeight + type.getWeightRatio() * amountToAdd, result);
  }

  @Test
  void shouldReturnTrueWhenWeightLimitExceeded() {
    // Given
    WeightCalculator calculator = new WeightCalculator();

    // When
    boolean result = calculator.isLimitExceeded(120, 100);

    // Then
    assertTrue(result);
  }

  @Test
  void shouldReturnFalseWhenWeightIsWithinLimit() {
    // Given
    WeightCalculator calculator = new WeightCalculator();

    // When
    boolean result = calculator.isLimitExceeded(80, 100);

    // Then
    assertFalse(result);
  }
}
