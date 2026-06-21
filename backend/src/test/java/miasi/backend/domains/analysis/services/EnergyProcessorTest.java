package miasi.backend.domains.analysis.services;

import miasi.backend.domains.analysis.types.core.Resource;
import miasi.backend.domains.analysis.types.modules.Module;
import miasi.backend.enums.ModuleState;
import miasi.backend.enums.ResourceType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EnergyProcessorTest {

  @Test
  void shouldDeactivateModulesWhenEnergyIsInsufficient() {
    // Given
    ProductionCalculator productionCalculator = mock(ProductionCalculator.class);
    DemandCalculator demandCalculator = mock(DemandCalculator.class);
    Module module = mock(Module.class);
    EnergyProcessor processor = new EnergyProcessor(productionCalculator, demandCalculator);

    when(productionCalculator.calculateModulesProduction(any()))
        .thenReturn(List.of(new Resource(ResourceType.ENERGY, 10)));
    when(demandCalculator.calculateModulesDemand(any()))
        .thenReturn(List.of(new Resource(ResourceType.ENERGY, 50)));

    // When
    boolean result = processor.process(0, List.of(module));

    // Then
    assertTrue(result);
    verify(module).setStatus(ModuleState.INACTIVE);
  }

  @Test
  void shouldReturnFalseWhenEnergyIsEnough() {// Given
    ProductionCalculator productionCalculator = mock();
    DemandCalculator demandCalculator = mock();
    EnergyProcessor processor = new EnergyProcessor(productionCalculator, demandCalculator);

    when(productionCalculator.calculateModulesProduction(any()))
        .thenReturn(List.of(new Resource(ResourceType.ENERGY, 100)));
    when(demandCalculator.calculateModulesDemand(any()))
        .thenReturn(List.of(new Resource(ResourceType.ENERGY, 50)));

    // When
    boolean result = processor.process(0, List.of());

    // Then
    assertFalse(result);
  }

  @Test
  void shouldReturnZeroWhenResourcesAreNull() {// Given
    EnergyProcessor processor = new EnergyProcessor(mock(), mock());

    // When
    float result = processor.getEnergyAmount(null);

    // Then
    assertEquals(0f, result);
  }

  @Test
  void shouldReturnEnergyAmount() {// Given
    EnergyProcessor processor = new EnergyProcessor(mock(), mock());

    List<Resource> resources = List.of(
        new Resource(ResourceType.FOOD, 10),
        new Resource(ResourceType.ENERGY, 25)
    );

    // When
    float result = processor.getEnergyAmount(resources);

    // Then
    assertEquals(25f, result);
  }
}