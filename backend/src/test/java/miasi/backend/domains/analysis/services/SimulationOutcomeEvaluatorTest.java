package miasi.backend.domains.analysis.services;

import miasi.backend.domains.analysis.simulation.Status;
import miasi.backend.domains.analysis.types.ResourceType;
import miasi.backend.domains.analysis.types.core.DailyState;
import miasi.backend.domains.analysis.types.core.ObservationType;
import miasi.backend.domains.analysis.types.core.Resource;
import miasi.backend.domains.analysis.types.input.MissionManifest;
import miasi.backend.domains.analysis.types.result.SimulationOutcome;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SimulationOutcomeEvaluatorTest {

  @Test
  void shouldReturnSuccessWhenNoResourceDeficitExists() {// Given
    SimulationOutcomeEvaluator evaluator =
        new SimulationOutcomeEvaluator();
    DailyState state = mock(DailyState.class);

    when(state.getWarehouse()).thenReturn(
        List.of(new Resource(ResourceType.OXYGEN, 10))
    );
    when(state.getObservations()).thenReturn(Set.of());

    MissionManifest manifest = mock(MissionManifest.class);

    // When
    SimulationOutcome result =
        evaluator.evaluate(
            List.of(state),
            manifest
        );

    // Then
    assertEquals(Status.SUCCESS, result.getStatus());
    assertNull(result.getDeathSol());
  }

  @Test
  void shouldDetectDeathDayWhenCriticalResourceIsNegative() {// Given
    SimulationOutcomeEvaluator evaluator = new SimulationOutcomeEvaluator();
    DailyState state = mock(DailyState.class);

    when(state.getSol()).thenReturn(15);
    when(state.getWarehouse()).thenReturn(
        List.of(new Resource(ResourceType.WATER, -1))
    );
    when(state.getObservations()).thenReturn(Set.of());

    // When
    SimulationOutcome result =
        evaluator.evaluate(
            List.of(state),
            mock(MissionManifest.class)
        );

    // Then
    assertEquals(15, result.getDeathSol());
    assertEquals(Status.FAILURE, result.getStatus());
  }

  @Test
  void shouldReturnEvacuationWhenRescueArrivesBeforeDeath() {// Given
    SimulationOutcomeEvaluator evaluator = new SimulationOutcomeEvaluator();
    DailyState sosDay = mock(DailyState.class);
    DailyState deathDay = mock(DailyState.class);
    MissionManifest manifest = mock(MissionManifest.class);

    when(sosDay.getSol()).thenReturn(5);
    when(sosDay.getObservations())
        .thenReturn(Set.of(ObservationType.EVACUATION_ALERT));
    when(sosDay.getWarehouse()).thenReturn(List.of());
    when(deathDay.getSol()).thenReturn(20);
    when(deathDay.getWarehouse()).thenReturn(
        List.of(new Resource(ResourceType.FOOD, -5))
    );
    when(deathDay.getObservations()).thenReturn(Set.of());
    when(manifest.getRescueSols()).thenReturn(10);

    // When
    SimulationOutcome result = evaluator.evaluate(List.of(sosDay, deathDay), manifest);

    // Then
    assertEquals(Status.EVACUATION, result.getStatus());
    assertEquals(15, result.getEvacuationSol());
  }
}