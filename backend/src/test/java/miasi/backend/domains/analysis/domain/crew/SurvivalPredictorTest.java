package miasi.backend.domains.analysis.domain.crew;

import miasi.backend.domains.analysis.domain.core.MissionManifest;
import miasi.backend.domains.analysis.domain.core.Resource;
import miasi.backend.domains.analysis.domain.core.ResourceType;
import miasi.backend.domains.analysis.domain.modules.ProductionCalculator;
import miasi.backend.domains.analysis.domain.schedule.Delivery;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SurvivalPredictorTest {
  @Mock
  DemandCalculator demandCalculator;
  @Mock
  ProductionCalculator productionCalculator;
  @InjectMocks
  SurvivalPredictor predictor;

  @Test
  void shouldReturnMinimalModeWhenCrewWillDie() {
    // Given
    MissionManifest manifest = mock(MissionManifest.class);

    when(demandCalculator.calculateCrewDemand(
        any(),
        eq(ConsumptionMode.OPTIMAL)
    ))
        .thenReturn(List.of(new Resource(ResourceType.OXYGEN, 100)));
    when(demandCalculator.calculateModulesDemand(any()))
        .thenReturn(List.of());
    when(productionCalculator.calculateModulesProduction(any()))
        .thenReturn(List.of());

    // When
    ConsumptionMode mode = predictor.evaluateCrewConsumptionMode(
        0,
        10,
        List.of(new Resource(ResourceType.OXYGEN, 10)),
        List.of(),
        manifest
    );

    // Then
    assertThat(mode).isEqualTo(ConsumptionMode.MINIMAL);
  }

  @Test
  void shouldReturnOptimalWhenResourcesAreEnough() {
    // Given
    MissionManifest manifest = mock(MissionManifest.class);

    when(demandCalculator.calculateCrewDemand(any(), any()))
        .thenReturn(List.of(new Resource(ResourceType.OXYGEN, 1)));
    when(demandCalculator.calculateModulesDemand(any()))
        .thenReturn(List.of());
    when(productionCalculator.calculateModulesProduction(any()))
        .thenReturn(List.of(new Resource(ResourceType.OXYGEN, 10)));

    // When
    ConsumptionMode mode = predictor.evaluateCrewConsumptionMode(
        0,
        10,
        List.of(new Resource(ResourceType.OXYGEN, 100)),
        List.of(),
        manifest);

    // Then
    assertThat(mode).isEqualTo(ConsumptionMode.OPTIMAL);
  }

  @Test
  void shouldReturnTrueWhenEvacuationIsNeededBecauseResourcesWillRunOut() {
    // Given
    MissionManifest manifest = mock(MissionManifest.class);
    when(demandCalculator.calculateCrewDemand(any(), eq(ConsumptionMode.MINIMAL)))
        .thenReturn(List.of(new Resource(ResourceType.OXYGEN, 100)));
    when(demandCalculator.calculateModulesDemand(any()))
        .thenReturn(List.of());
    when(productionCalculator.calculateModulesProduction(any()))
        .thenReturn(List.of());

    // When
    boolean evacuationNeeded = predictor.checkIfEvacuationIsNeeded(
        0,
        10,
        List.of(new Resource(ResourceType.OXYGEN, 20)),
        List.of(),
        manifest
    );

    // Then
    assertThat(evacuationNeeded).isTrue();
  }

  @Test
  void shouldReturnFalseWhenEvacuationIsNotNeededBecauseResourcesAreEnough() {
    // Given
    MissionManifest manifest = mock(MissionManifest.class);
    when(demandCalculator.calculateCrewDemand(any(), eq(ConsumptionMode.MINIMAL)))
        .thenReturn(List.of(new Resource(ResourceType.OXYGEN, 1)));
    when(demandCalculator.calculateModulesDemand(any()))
        .thenReturn(List.of());
    when(productionCalculator.calculateModulesProduction(any()))
        .thenReturn(List.of(new Resource(ResourceType.OXYGEN, 10)));

    // When
    boolean evacuationNeeded = predictor.checkIfEvacuationIsNeeded(
        0,
        10,
        List.of(new Resource(ResourceType.OXYGEN, 100)),
        List.of(),
        manifest
    );

    // Then
    assertThat(evacuationNeeded).isFalse();
  }

  @Test
  void shouldNotRequestEvacuationWhenDeliveryContainsRequiredResource() {
    // Given
    MissionManifest manifest = mock(MissionManifest.class);
    Resource oxygenDelivery = new Resource(ResourceType.OXYGEN, 5000);
    Delivery delivery = mock(Delivery.class);

    when(delivery.getSol()).thenReturn(1);
    when(delivery.getResources())
        .thenReturn(List.of(oxygenDelivery));
    when(manifest.getDeliveries())
        .thenReturn(List.of(delivery));

    when(demandCalculator.calculateCrewDemand(any(), eq(ConsumptionMode.MINIMAL)))
        .thenReturn(List.of(new Resource(ResourceType.OXYGEN, 10)));
    when(demandCalculator.calculateModulesDemand(any()))
        .thenReturn(List.of());
    when(productionCalculator.calculateModulesProduction(any()))
        .thenReturn(List.of());

    // When
    boolean evacuationNeeded = predictor.checkIfEvacuationIsNeeded(
        0,
        10,
        List.of(new Resource(ResourceType.OXYGEN, 20)),
        List.of(),
        manifest
    );

    // Then
    assertThat(evacuationNeeded).isFalse();
  }
}