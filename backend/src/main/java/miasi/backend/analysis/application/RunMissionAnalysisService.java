package miasi.backend.analysis.application;

import java.util.List;
import lombok.RequiredArgsConstructor;
import miasi.backend.analysis.application.port.in.RunMissionAnalysisPort;
import miasi.backend.analysis.application.port.out.AnalysisEventPublisherPort;
import miasi.backend.analysis.application.port.out.AnalysisResultRepositoryPort;
import miasi.backend.domains.analysis.baseline.BaselineAnalysisCompletedEvent;
import miasi.backend.domains.analysis.baseline.BaselineAnalysisSession;
import miasi.backend.domains.analysis.baseline.BaselineAnalyzer;
import miasi.backend.domains.analysis.simulation.MissionFailureDetectedEvent;
import miasi.backend.domains.analysis.simulation.SimulationAnalysisCompletedEvent;
import miasi.backend.domains.analysis.simulation.SimulationAnalysisSession;
import miasi.backend.domains.analysis.simulation.SimulationAnalyzer;
import miasi.backend.domains.analysis.simulation.Status;
import miasi.backend.domains.analysis.types.input.MissionManifest;

@RequiredArgsConstructor
public class RunMissionAnalysisService implements RunMissionAnalysisPort {
  private final BaselineAnalyzer baselineAnalyzer;
  private final SimulationAnalyzer simulationAnalyzer;
  private final AnalysisResultRepositoryPort resultRepository;
  private final AnalysisEventPublisherPort eventPublisher;

  @Override
  public MissionAnalysisResult run(MissionManifest manifest) {
    if (manifest == null) {
      throw new IllegalArgumentException("Mission manifest is required");
    }
    if (manifest.getId() == null) {
      throw new IllegalArgumentException("Mission manifest id is required");
    }

    BaselineAnalysisSession baselineSession = baselineAnalyzer.analyze(manifest);
    eventPublisher.publishBaselineAnalysisCompleted(
        BaselineAnalysisCompletedEvent.create(
            manifest.getId(),
            baselineSession.getDailyStates(),
            baselineSession.getConfiguration()));

    SimulationAnalysisSession simulationSession =
        simulationAnalyzer.analyze(manifest, baselineSession, safeThreats(manifest));
    eventPublisher.publishSimulationAnalysisCompleted(
        SimulationAnalysisCompletedEvent.create(
            manifest.getId(),
            simulationSession.getIdealVariant(),
            simulationSession.getRealVariant()));

    if (simulationSession.getRealVariant().getStatus() == Status.FAILURE) {
      eventPublisher.publishMissionFailureDetected(
          MissionFailureDetectedEvent.create(manifest.getId(), simulationSession.getRealVariant()));
    }

    MissionAnalysisResult result = new MissionAnalysisResult(baselineSession, simulationSession);
    resultRepository.save(manifest.getId(), result);
    return result;
  }

  private List<miasi.backend.domains.analysis.types.schedule.Threat> safeThreats(
      MissionManifest manifest) {
    return manifest.getThreats() == null ? List.of() : List.copyOf(manifest.getThreats());
  }
}
