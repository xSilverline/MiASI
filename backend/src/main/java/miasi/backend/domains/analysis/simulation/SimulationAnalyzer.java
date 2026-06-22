package miasi.backend.domains.analysis.simulation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import miasi.backend.domains.analysis.baseline.BaselineAnalysisSession;
import miasi.backend.domains.analysis.services.SimulationOutcomeEvaluator;
import miasi.backend.domains.analysis.services.TimelineSimulator;
import miasi.backend.domains.analysis.types.core.DailyState;
import miasi.backend.domains.analysis.types.input.MissionManifest;
import miasi.backend.domains.analysis.types.result.OptimalConfiguration;
import miasi.backend.domains.analysis.types.result.SimulationOutcome;
import miasi.backend.domains.analysis.types.schedule.Threat;

@RequiredArgsConstructor
public class SimulationAnalyzer {

  private final TimelineSimulator timelineSimulator;
  private final SimulationOutcomeEvaluator outcomeEvaluator;

  public SimulationAnalysisSession analyze(
      MissionManifest manifest, BaselineAnalysisSession baselineSession, List<Threat> threats) {
    if (manifest == null) {
      throw new IllegalArgumentException("Mission manifest is required");
    }
    if (baselineSession == null || baselineSession.getConfiguration() == null) {
      throw new IllegalArgumentException("Baseline session with configuration is required");
    }

    OptimalConfiguration configuration = baselineSession.getConfiguration();
    MissionManifest idealManifest = copyManifestWithThreats(manifest, List.of());
    MissionManifest realManifest = copyManifestWithThreats(manifest, safeThreats(threats));

    List<DailyState> idealTimeline =
        timelineSimulator.simulate(
            idealManifest,
            new ArrayList<>(configuration.getOptimalModules()),
            new ArrayList<>(configuration.getStartingResources()));
    List<DailyState> realTimeline =
        timelineSimulator.simulate(
            realManifest,
            new ArrayList<>(configuration.getOptimalModules()),
            new ArrayList<>(configuration.getStartingResources()));

    SimulationVariant idealVariant = toVariant(VariantType.IDEAL, idealTimeline, idealManifest);
    SimulationVariant realVariant = toVariant(VariantType.REAL, realTimeline, realManifest);
    Status sessionStatus =
        realVariant.getStatus() == Status.FAILURE ? Status.FAILURE : idealVariant.getStatus();

    return new SimulationAnalysisSession(
        UUID.randomUUID(), sessionStatus.name(), idealVariant, realVariant);
  }

  private SimulationVariant toVariant(
      VariantType type, List<DailyState> timeline, MissionManifest manifest) {
    SimulationOutcome outcome = outcomeEvaluator.evaluate(timeline, manifest);
    SimulationVariant variant = new SimulationVariant(type, outcome.getStatus(), timeline);
    variant.setDeathSol(outcome.getDeathSol());
    variant.setEvacuationSol(outcome.getEvacuationSol());
    return variant;
  }

  private MissionManifest copyManifestWithThreats(MissionManifest manifest, List<Threat> threats) {
    return new MissionManifest(
        manifest.getId(),
        manifest.getDurationSols(),
        manifest.getRescueSols(),
        manifest.getMaxWeightSolZero(),
        safeList(manifest.getCrew()),
        safeList(manifest.getCatalog()),
        safeList(manifest.getDeliveries()),
        threats);
  }

  private List<Threat> safeThreats(List<Threat> threats) {
    return threats == null ? List.of() : List.copyOf(threats);
  }

  private <T> List<T> safeList(List<T> list) {
    return list == null ? Collections.emptyList() : List.copyOf(list);
  }
}
