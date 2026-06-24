package miasi.backend.analysis.application.port.out;

import miasi.backend.analysis.domain.model.baseline.BaselineAnalysisCompletedEvent;
import miasi.backend.analysis.domain.model.simulation.MissionFailureDetectedEvent;
import miasi.backend.analysis.domain.model.simulation.SimulationAnalysisCompletedEvent;

public interface AnalysisEventPublisherPort {
  void publishBaselineAnalysisCompleted(BaselineAnalysisCompletedEvent event);

  void publishSimulationAnalysisCompleted(SimulationAnalysisCompletedEvent event);

  void publishMissionFailureDetected(MissionFailureDetectedEvent event);
}
