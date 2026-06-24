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
      MissionManifest baseManifest, // Potrzebujemy bazowego manifestu (czas trwania, załoga itp.)
      List<Threat> threats,
      String scheduleId) {

    // 1. Klonujemy manifest i dodajemy do niego awarie z harmonogramu
    // (Zakładam, że masz metodę kopiującą lub setter w Manifeście, żeby nie psuć oryginału)
    MissionManifest threatManifest = baseManifest.copyWithThreats(threats);

    // 2. Wyciągamy "zatwierdzony" sprzęt i zapasy z Fazy 2
    List<Module> modules = nominalSession.getCustomizedModules();
    List<Resource> supplies = nominalSession.getCustomizedSupplies();

    // 3. Odpalamy symulator Z AWARIAMI (Scenariusz REAL)
    List<DailyState> realTimeline = timelineSimulator.simulate(threatManifest, modules, supplies);

    // 4. Oceniamy, jak załoga poradziła sobie z awariami
    SimulationOutcome realOutcome = outcomeEvaluator.evaluate(realTimeline, threatManifest);
    SimulationVariant realVariant = new SimulationVariant(VariantType.REAL, realTimeline,
        realOutcome);

    // 5. Pakujemy wszystko w jedną wielką Sesję A/B (Porównanie IDEAL vs REAL)
    return new ScenariosAnalysisSession(
        UUID.randomUUID().toString(),
        nominalSession.getId(),
        scheduleId,
        threats,
        nominalSession.getNominalVariant(), // Wariant IDEALNY (skopiowany z Fazy 2)
        realVariant,                        // Wariant Z AWARIAMI (wyliczony przed chwilą)
        LocalDateTime.now()
    );
  }
}