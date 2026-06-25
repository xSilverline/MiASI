package miasi.backend.domains.analysis.domain.energy;

import miasi.backend.domains.analysis.domain.core.Resource;
import miasi.backend.domains.analysis.domain.core.ResourceType;
import miasi.backend.domains.analysis.domain.crew.DemandCalculator;
import miasi.backend.domains.analysis.domain.modules.Module;
import miasi.backend.domains.analysis.domain.modules.ModuleState;
import miasi.backend.domains.analysis.domain.modules.ProductionCalculator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PowerGridPlannerTest {
  @Mock
  ProductionCalculator productionCalculator;
  @Mock
  DemandCalculator demandCalculator;

  @InjectMocks
  PowerGridPlanner planner;

  @Test
  void shouldNotAddGeneratorWhenPowerBalanceIsPositive() {
    // Given
    Module module = createModule("M1");
    List<Module> currentModules = new ArrayList<>(List.of(module));
    List<Module> catalog = List.of(module);

    when(productionCalculator.calculateModulesProduction(any()))
        .thenReturn(List.of(new Resource(ResourceType.ENERGY, 100)));
    when(demandCalculator.calculateModulesDemand(any()))
        .thenReturn(List.of(new Resource(ResourceType.ENERGY, 50)));

    // When
    planner.resolvePowerDeficit(currentModules, catalog);

    // Then
    assertThat(currentModules)
        .hasSize(1);
    verify(productionCalculator, atLeastOnce())
        .calculateModulesProduction(currentModules);
  }

  @Test
  void shouldAddGeneratorWhenPowerDeficitExists() {
    // Given
    Module generator = createModule("GENERATOR");
    List<Module> currentModules = new ArrayList<>();
    List<Module> catalog = List.of(generator);

    when(productionCalculator.calculateModulesProduction(any()))
        .thenAnswer(invocation -> {
          List<Module> modules = invocation.getArgument(0);
          if (modules.isEmpty()) {
            return List.of(new Resource(ResourceType.ENERGY, 0));
          }
          return List.of(new Resource(ResourceType.ENERGY, 100));
        });
    when(demandCalculator.calculateModulesDemand(any()))
        .thenReturn(List.of(new Resource(ResourceType.ENERGY, 50)));

    // When
    planner.resolvePowerDeficit(currentModules, catalog);

    // Then
    assertThat(currentModules).hasSize(1);
    assertThat(currentModules.getFirst().getId()).isEqualTo("GENERATOR");
  }

  @Test
  void shouldSelectGeneratorWithHighestEnergyProduction() {
    // Given
    Module weakGenerator = createModule("WEAK");
    Module strongGenerator = createModule("STRONG");
    List<Module> currentModules = new ArrayList<>();
    List<Module> catalog = List.of(weakGenerator, strongGenerator);

    when(productionCalculator.calculateModulesProduction(any()))
        .thenAnswer(invocation -> {
          List<Module> modules = invocation.getArgument(0);
          if (modules.isEmpty()) {
            return List.of();
          }
          Module module = modules.getFirst();
          if ("STRONG".equals(module.getId())) {
            return List.of(new Resource(ResourceType.ENERGY, 2000));
          }
          return List.of(new Resource(ResourceType.ENERGY, 100));
        });

    when(demandCalculator.calculateModulesDemand(any()))
        .thenReturn(List.of(new Resource(ResourceType.ENERGY, 10)));

    // When
    planner.resolvePowerDeficit(currentModules, catalog);

    // Then
    assertThat(currentModules).hasSize(1);
    assertThat(currentModules.getFirst().getId())
        .isEqualTo("STRONG");
  }

  @Test
  void shouldStopWhenThereIsNoGeneratorAvailable() {
    // Given
    Module consumer = createModule("CONSUMER");
    List<Module> currentModules = new ArrayList<>();
    List<Module> catalog = List.of(consumer);

    when(productionCalculator.calculateModulesProduction(any()))
        .thenReturn(List.of(new Resource(ResourceType.ENERGY, 0)));
    when(demandCalculator.calculateModulesDemand(any()))
        .thenReturn(List.of(new Resource(ResourceType.ENERGY, 100)));

    // When
    planner.resolvePowerDeficit(currentModules, catalog);

    // Then
    assertThat(currentModules).isEmpty();
  }

  private Module createModule(String id) {
    return new Module(id, id, 10, 0, 5, List.of(), List.of(), ModuleState.ACTIVE, 1f);
  }
}