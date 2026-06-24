package miasi.backend.domains.analysis.domain._simulation;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import miasi.backend.domains.analysis.domain.core.DailyState;
import miasi.backend.domains.analysis.domain.core.MissionManifest;
import miasi.backend.domains.analysis.domain.core.Resource;
import miasi.backend.domains.analysis.domain.core.VariantType;
import miasi.backend.domains.analysis.domain.modules.Module;
import miasi.backend.domains.analysis.domain.schedule.Threat;

@RequiredArgsConstructor
public class ScenariosSimulator {

  private final TimelineSimulator timelineSimulator;
  private final SimulationOutcomeEvaluator outcomeEvaluator;

  public ScenariosAnalysisSession analyze(
      NominalSimulationSession nominalSession,
      MissionManifest baseManifest,
      List<Threat> threats,
      String scheduleId) {

    MissionManifest threatManifest = baseManifest.copyWithThreats(threats);

    List<Module> modules = nominalSession.getCustomizedModules();
    List<Resource> supplies = nominalSession.getCustomizedSupplies();

    List<DailyState> realTimeline = timelineSimulator.simulate(threatManifest, modules, supplies);

    SimulationOutcome realOutcome = outcomeEvaluator.evaluate(realTimeline, threatManifest);
    SimulationVariant realVariant = new SimulationVariant(VariantType.REAL, realTimeline,
        realOutcome);

    return new ScenariosAnalysisSession(
        UUID.randomUUID().toString(),
        nominalSession.getId(),
        scheduleId,
        threats,
        nominalSession.getNominalVariant(),
        realVariant,
        LocalDateTime.now()
    );
  }
}