package miasi.backend.domains.analysis.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import miasi.backend.domains.analysis.domain.core.Resource;
import miasi.backend.domains.analysis.domain.core.ResourceType;
import miasi.backend.domains.analysis.domain.crew.DemandCalculator;
import miasi.backend.domains.analysis.domain.energy.PowerGridSimulator;
import miasi.backend.domains.analysis.domain.modules.Module;
import miasi.backend.domains.analysis.domain.modules.ModuleState;
import miasi.backend.domains.analysis.domain.modules.ProductionCalculator;
import org.junit.jupiter.api.Test;

class PowerGridSimulatorTest {

  @Test
  void shouldDeactivateModulesWhenEnergyIsInsufficient() {
    // Given
    ProductionCalculator productionCalculator = mock(ProductionCalculator.class);
    DemandCalculator demandCalculator = mock(DemandCalculator.class);
    Module module = mock(Module.class);
    PowerGridSimulator processor = new PowerGridSimulator(productionCalculator, demandCalculator);

    when(productionCalculator.calculateModulesProduction(any()))
        .thenReturn(List.of(new Resource(ResourceType.ENERGY, 10)));
    when(demandCalculator.calculateModulesDemand(any()))
        .thenReturn(List.of(new Resource(ResourceType.ENERGY, 50)));

    when(module.withStatus(any())).thenReturn(module);
    List<Module> currentModules = new ArrayList<>(List.of(module));

    // When
    boolean result = processor.process(0, List.of(module));

    // Then
    assertTrue(result);
    verify(module).withStatus(ModuleState.INACTIVE);
  }

  @Test
  void shouldReturnFalseWhenEnergyIsEnough() {// Given
    ProductionCalculator productionCalculator = mock();
    DemandCalculator demandCalculator = mock();
    PowerGridSimulator processor = new PowerGridSimulator(productionCalculator, demandCalculator);

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
    PowerGridSimulator processor = new PowerGridSimulator(mock(), mock());

    // When
    float result = processor.getEnergyAmount(null);

    // Then
    assertEquals(0f, result);
  }

  @Test
  void shouldReturnEnergyAmount() {// Given
    PowerGridSimulator processor = new PowerGridSimulator(mock(), mock());

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