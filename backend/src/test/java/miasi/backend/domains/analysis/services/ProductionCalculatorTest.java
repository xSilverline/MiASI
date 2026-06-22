package miasi.backend.domains.analysis.services;

import miasi.backend.domains.analysis.types.ResourceType;
import miasi.backend.domains.analysis.types.core.Resource;
import miasi.backend.domains.analysis.types.modules.Module;
import miasi.backend.domains.analysis.types.modules.ModuleState;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ProductionCalculatorTest {

  @Test
  void shouldCalculateProductionOnlyForActiveModules() {// Given
    ProductionCalculator calculator = new ProductionCalculator();

    Module module = mock(Module.class);

    when(module.getStatus()).thenReturn(ModuleState.ACTIVE);
    when(module.getEfficiency()).thenReturn(0.8f);
    when(module.getProduction()).thenReturn(
        List.of(new Resource(ResourceType.OXYGEN, 10))
    );

    // When
    List<Resource> result = calculator.calculateModulesProduction(
        List.of(module)
    );

    // Then
    assertEquals(8, result.getFirst().getAmount());
  }

  @Test
  void shouldIgnoreInactiveModules() {// Given
    ProductionCalculator calculator = new ProductionCalculator();
    Module module = mock(Module.class);

    when(module.getStatus()).thenReturn(ModuleState.INACTIVE);

    // When
    List<Resource> result = calculator.calculateModulesProduction(
        List.of(module)
    );

    // Then
    assertTrue(result.isEmpty());
  }
}
