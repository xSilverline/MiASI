package miasi.backend.domains.analysis.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
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
    PowerGridSimulator processor = new PowerGridSimulator(productionCalculator, demandCalculator);

    when(productionCalculator.calculateModulesProduction(any()))
        .thenReturn(List.of(new Resource(ResourceType.ENERGY, 10f)));
    when(demandCalculator.calculateModulesDemand(any()))
        .thenReturn(List.of(new Resource(ResourceType.ENERGY, 50f)));

    Module realModule = Module.builder()
        .name("Wiertnica")
        .status(ModuleState.ACTIVE)
        .build();

    List<Module> currentModules = new ArrayList<>(List.of(realModule));

    // When
    boolean blackoutOccurred = processor.process(0f, currentModules);

    // Then
    assertTrue(blackoutOccurred, "Powinien wystąpić blackout (zwrócić true)");

    assertEquals(ModuleState.INACTIVE, currentModules.get(0).getStatus(),
        "Moduł powinien zostać wyłączony z powodu braku energii");
  }

  @Test
  void shouldReturnFalseWhenEnergyIsEnough() {
    // Given
    ProductionCalculator productionCalculator = mock(ProductionCalculator.class);
    DemandCalculator demandCalculator = mock(DemandCalculator.class);
    PowerGridSimulator processor = new PowerGridSimulator(productionCalculator, demandCalculator);

    when(productionCalculator.calculateModulesProduction(any()))
        .thenReturn(List.of(new Resource(ResourceType.ENERGY, 100f)));
    when(demandCalculator.calculateModulesDemand(any()))
        .thenReturn(List.of(new Resource(ResourceType.ENERGY, 50f)));

    Module realModule = Module.builder()
        .name("Farma Hydroponiczna")
        .status(ModuleState.ACTIVE)
        .build();

    List<Module> currentModules = new ArrayList<>(List.of(realModule));

    // When
    boolean blackoutOccurred = processor.process(0f, currentModules);

    // Then
    assertFalse(blackoutOccurred, "Blackout nie powinien wystąpić (zwrócić false)");
    assertEquals(ModuleState.ACTIVE, currentModules.get(0).getStatus(),
        "Moduł powinien pozostać aktywny");
  }

  @Test
  void shouldReturnZeroWhenResourcesAreNull() {
    // Given
    PowerGridSimulator processor = new PowerGridSimulator(mock(), mock());

    // When
    float result = processor.getEnergyAmount(null);

    // Then
    assertEquals(0f, result, "Dla null lista zasobów powinna zwrócić 0 energii");
  }

  @Test
  void shouldReturnEnergyAmount() {
    // Given
    PowerGridSimulator processor = new PowerGridSimulator(mock(), mock());

    List<Resource> resources = List.of(
        new Resource(ResourceType.FOOD, 10),
        new Resource(ResourceType.ENERGY, 25)
    );

    // When
    float result = processor.getEnergyAmount(resources);

    // Then
    assertEquals(25, result, "Powinien poprawnie wyciągnąć wartość energii z listy");
  }
}