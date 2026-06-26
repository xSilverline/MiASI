package miasi.backend.domains.analysis.domain._payload;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import miasi.backend.domains.analysis.domain._simulation.SimulationOutcomeEvaluator;
import miasi.backend.domains.analysis.domain._simulation.TimelineSimulator;
import miasi.backend.domains.analysis.domain.core.DailyState;
import miasi.backend.domains.analysis.domain.core.MissionManifest;
import miasi.backend.domains.analysis.domain.energy.PowerGridPlanner;
import miasi.backend.domains.analysis.domain.modules.Module;
import miasi.backend.domains.analysis.domain.modules.ModuleState;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PayloadWeightOptimizerTest {

  @Mock
  WeightCalculator weightCalculator;
  @Mock
  TimelineSimulator timelineSimulator;
  @Mock
  PowerGridPlanner powerGridPlanner;
  @Mock
  SimulationOutcomeEvaluator evaluator;

  @InjectMocks
  PayloadWeightOptimizer optimizer;

  @Test
  void shouldReturnConfigurationWithMandatoryModulesOnly_whenNoBetterModuleExists() {
    // Given
    MissionManifest manifest = mock(MissionManifest.class);
    Module mandatoryModule =
        new Module(
            "M1",
            "LifeSupport",
            100,
            1,
            1,
            List.of(),
            List.of(),
            ModuleState.ACTIVE,
            1f
        );
    Module optionalModule =
        new Module(
            "M2",
            "Solar",
            200,
            0,
            1,
            List.of(),
            List.of(),
            ModuleState.ACTIVE,
            1f
        );
    List<Module> catalog = List.of(mandatoryModule, optionalModule);

    when(timelineSimulator.simulate(any(), any(), any()))
        .thenReturn(List.of(mock(DailyState.class)));
//    when(evaluator.calculateMinimumSurvivalSupplies(any()))
//        .thenReturn(List.of());
    when(weightCalculator.calculateTotalWeight(any(), any()))
        .thenReturn(100f);

    // When
    OptimalConfiguration result =
        optimizer.optimizeConfiguration(
            manifest,
            catalog
        );

    // Then
    assertThat(result)
        .isNotNull();
    assertThat(result.getOptimalModules())
        .hasSize(1);
    assertThat(result.getTotalWeight())
        .isEqualTo(100f);
    verify(powerGridPlanner, times(2))
        .resolvePowerDeficit(any(), eq(catalog));
  }

  @Test
  void shouldAddModule_whenItReducesTotalWeight() {
    // Given
    MissionManifest manifest = mock(MissionManifest.class);
    Module baseModule =
        new Module(
            "BASE",
            "Base",
            100,
            1,
            1,
            List.of(),
            List.of(),
            ModuleState.ACTIVE,
            1f
        );
    Module betterModule =
        new Module(
            "ADD",
            "Optimizer",
            50,
            0,
            1,
            List.of(),
            List.of(),
            ModuleState.ACTIVE,
            1f
        );
    List<Module> catalog = List.of(baseModule, betterModule);

    when(timelineSimulator.simulate(any(), any(), any()))
        .thenReturn(List.of(mock(DailyState.class)));
//    when(evaluator.calculateMinimumSurvivalSupplies(any()))
//        .thenReturn(List.of(
//            new Resource(ResourceType.OXYGEN, 0)));
    when(weightCalculator.calculateTotalWeight(any(), any()))
        .thenReturn(100f, 70f);

    // When
    OptimalConfiguration result =
        optimizer.optimizeConfiguration(manifest, catalog);

    // Then
    assertThat(result.getTotalWeight())
        .isEqualTo(70f);
    assertThat(result.getOptimalModules())
        .extracting(Module::getId)
        .contains("ADD");
//    verify(weightCalculator, atLeast(2))
//        .calculateTotalWeight(any(), any());
  }

  @Test
  void shouldInitializeMultipleMandatoryModules() {
    // Given
    MissionManifest manifest = mock(MissionManifest.class);
    Module module =
        new Module(
            "M1",
            "Generator",
            20,
            3,
            5,
            List.of(),
            List.of(),
            ModuleState.ACTIVE,
            1f
        );

    when(timelineSimulator.simulate(any(), any(), any()))
        .thenReturn(List.of(mock(DailyState.class)));
//    when(evaluator.calculateMinimumSurvivalSupplies(any()))
//        .thenReturn(List.of());
    when(weightCalculator.calculateTotalWeight(any(), any()))
        .thenReturn(60f);

    // When
    OptimalConfiguration result =
        optimizer.optimizeConfiguration(manifest, List.of(module));

    // Then
    assertThat(result.getOptimalModules()).hasSize(3);
    assertThat(result.getOptimalModules()).allMatch(m -> m.getId().equals("M1"));
  }

  @Test
  void shouldCallSimulationForEveryConfigurationCheck() {
    // Given
    MissionManifest manifest = mock(MissionManifest.class);
    Module module =
        new Module(
            "M1",
            "Module",
            10,
            1,
            1,
            List.of(),
            List.of(),
            ModuleState.ACTIVE,
            1f
        );

    when(timelineSimulator.simulate(any(), any(), any()))
        .thenReturn(List.of(mock(DailyState.class)));
//    when(evaluator.calculateMinimumSurvivalSupplies(any()))
//        .thenReturn(List.of());
    when(weightCalculator.calculateTotalWeight(any(), any()))
        .thenReturn(10f);
    // When
    optimizer.optimizeConfiguration(manifest, List.of(module));

    // Then
    verify(timelineSimulator).simulate(eq(manifest), any(), any());
//    verify(evaluator).calculateMinimumSurvivalSupplies(any());
  }

  @Test
  void shouldNotExceedModuleMaxCount_evenIfItReducesWeight() {
    // Given
    MissionManifest manifest = mock(MissionManifest.class);
    Module superEfficientModule =
        new Module(
            "MAGIC",
            "Super Module",
            10, // Niska waga
            0,  // minCount
            2,  // maxCount - możemy dodać maksymalnie 2 sztuki!
            List.of(),
            List.of(),
            ModuleState.ACTIVE,
            1f
        );

    List<Module> catalog = List.of(superEfficientModule);

    // Symulacja działa
    when(timelineSimulator.simulate(any(), any(), any()))
        .thenReturn(List.of(mock(DailyState.class)));

    // 1 iteracja (0 modułów): 1000f
    // 2 iteracja (1 moduł): 800f
    // 3 iteracja (2 moduły): 600f
    // 4 iteracja (próba dodania 3 modułu, która powinna zostać zablokowana przez maxCount)
    when(weightCalculator.calculateTotalWeight(any(), any()))
        .thenReturn(1000f, 800f, 600f, 400f);

    // When
    OptimalConfiguration result = optimizer.optimizeConfiguration(manifest, catalog);

    // Then
    // Zapewniamy, że pomimo tego, że kolejna iteracja dałaby wagę 400f,
    // algorytm zatrzymał się po dodaniu 2 modułów (bo maxCount = 2)
    assertThat(result.getOptimalModules())
        .hasSize(2)
        .extracting(Module::getId)
        .containsOnly("MAGIC");

    assertThat(result.getTotalWeight())
        .isEqualTo(600f); // Waga dla dokładnie dwóch modułów
  }

}