package miasi.backend.domains.analisis.ports;

import miasi.backend.domains.analisis.baseline.BaselineAnalysisCompletedEvent;
import miasi.backend.domains.analisis.simulation.MissionFailureDetectedEvent;
import miasi.backend.domains.analisis.simulation.SimulationAnalysisCompletedEvent;

public interface IAnalysisEventPublisherPort {
  void publishBaselineAnalysisCompleted(BaselineAnalysisCompletedEvent event);

  void publishSimulationAnalysisCompleted(SimulationAnalysisCompletedEvent event);

  void publishMissionFailureDetected(MissionFailureDetectedEvent event);
}
