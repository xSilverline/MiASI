package miasi.backend.domains.analysis.domain._simulation;

import miasi.backend.domains.analysis.domain.core.DailyState;
import miasi.backend.domains.analysis.domain.core.MissionManifest;
import miasi.backend.domains.analysis.domain.core.Resource;
import miasi.backend.domains.analysis.domain.core.VariantType;
import miasi.backend.domains.analysis.domain.modules.Module;
import miasi.backend.domains.analysis.domain.schedule.Threat;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class ScenariosSimulatorTest {

  @Mock
  TimelineSimulator timelineSimulator;

  @Mock
  SimulationOutcomeEvaluator outcomeEvaluator;

  @Mock
  NominalSimulationSession nominalSession;

  @Mock
  MissionManifest manifest;

  @Mock
  Threat threat;

  @Mock
  Module module;

  @Mock
  Resource resource;

  @Mock
  DailyState dailyState;

  @Mock
  SimulationOutcome outcome;

  @InjectMocks
  ScenariosSimulator simulator;


  @Test
  void shouldCreateRealScenarioAnalysisWhenSimulationCompleted() {

    // Given

    List<Module> modules = List.of(module);
    List<Resource> supplies = List.of(resource);
    List<Threat> threats = List.of(threat);
    List<DailyState> timeline = List.of(dailyState);

    when(nominalSession.getCustomizedModules())
        .thenReturn(modules);

    when(nominalSession.getCustomizedSupplies())
        .thenReturn(supplies);

    when(nominalSession.getId())
        .thenReturn("session-1");

    when(nominalSession.getNominalVariant())
        .thenReturn(mock(SimulationVariant.class));

    when(manifest.copyWithThreats(threats))
        .thenReturn(manifest);

    when(timelineSimulator.simulate(
        eq(manifest),
        eq(modules),
        eq(supplies)
    ))
        .thenReturn(timeline);

    when(outcomeEvaluator.evaluate(
        eq(timeline),
        eq(manifest)
    ))
        .thenReturn(outcome);


    // When

    ScenariosAnalysisSession result =
        simulator.analyze(
            nominalSession,
            manifest,
            threats,
            "schedule-1"
        );


    // Then

    assertThat(result)
        .isNotNull();
    assertThat(result.getScheduleId())
        .isEqualTo("schedule-1");
    assertThat(result.getNominalSessionId())
        .isEqualTo("session-1");
    assertThat(result.getRealVariant())
        .isNotNull();
    assertThat(result.getRealVariant().getType())
        .isEqualTo(VariantType.REAL);

    verify(manifest).copyWithThreats(threats);
    verify(timelineSimulator)
        .simulate(
            manifest,
            modules,
            supplies
        );
    verify(outcomeEvaluator)
        .evaluate(
            timeline,
            manifest
        );
  }

  @Test
  void shouldUseThreatsFromInputWhenCreatingThreatScenario() {
    // Given
    List<Threat> threats = List.of(threat);

    when(manifest.copyWithThreats(threats))
        .thenReturn(manifest);
    when(nominalSession.getCustomizedModules())
        .thenReturn(List.of());
    when(nominalSession.getCustomizedSupplies())
        .thenReturn(List.of());
    when(nominalSession.getId())
        .thenReturn("id");
    when(nominalSession.getNominalVariant())
        .thenReturn(mock(SimulationVariant.class));
    when(timelineSimulator.simulate(any(), any(), any()))
        .thenReturn(List.of());
    when(outcomeEvaluator.evaluate(any(), any()))
        .thenReturn(outcome);

    // When
    ScenariosAnalysisSession result =
        simulator.analyze(
            nominalSession,
            manifest,
            threats,
            "test"
        );

    // Then
    assertThat(result.getAppliedThreats())
        .containsExactly(threat);
    verify(manifest)
        .copyWithThreats(threats);
  }

  @Test
  void shouldPassCustomizedModulesAndSuppliesToTimelineSimulator() {

    // Given
    List<Module> modules = List.of(module);
    List<Resource> supplies = List.of(resource);

    when(nominalSession.getCustomizedModules())
        .thenReturn(modules);
    when(nominalSession.getCustomizedSupplies())
        .thenReturn(supplies);
    when(manifest.copyWithThreats(any()))
        .thenReturn(manifest);
    when(timelineSimulator.simulate(any(), any(), any()))
        .thenReturn(List.of());
    when(outcomeEvaluator.evaluate(any(), any()))
        .thenReturn(outcome);
    when(nominalSession.getId())
        .thenReturn("123");
    when(nominalSession.getNominalVariant())
        .thenReturn(mock(SimulationVariant.class));

    // When
    simulator.analyze(
        nominalSession,
        manifest,
        List.of(),
        "schedule"
    );

    // Then
    verify(timelineSimulator)
        .simulate(manifest, modules, supplies);
  }
}