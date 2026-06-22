package miasi.backend.analysis.application.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import miasi.backend.analysis.application.model.MissionAnalysisResult;
import miasi.backend.analysis.application.port.in.RunMissionAnalysisPort;
import miasi.backend.analysis.application.port.out.AnalysisEventPublisherPort;
import miasi.backend.analysis.application.port.out.AnalysisResultRepositoryPort;
import miasi.backend.analysis.domain.model.baseline.BaselineAnalysisCompletedEvent;
import miasi.backend.analysis.domain.model.baseline.BaselineAnalysisSession;
import miasi.backend.analysis.domain.model.input.MissionManifest;
import miasi.backend.analysis.domain.model.simulation.MissionFailureDetectedEvent;
import miasi.backend.analysis.domain.model.simulation.SimulationAnalysisCompletedEvent;
import miasi.backend.analysis.domain.model.simulation.SimulationAnalysisSession;
import miasi.backend.analysis.domain.model.simulation.Status;
import miasi.backend.analysis.domain.service.baseline.BaselineAnalyzer;
import miasi.backend.analysis.domain.service.simulation.SimulationAnalyzer;

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

  private List<miasi.backend.analysis.domain.model.schedule.Threat> safeThreats(
      MissionManifest manifest) {
    return manifest.getThreats() == null ? List.of() : List.copyOf(manifest.getThreats());
  }
}
