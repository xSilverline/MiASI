package miasi.backend.domains.analisis.ports;


import miasi.backend.domains.analysis.baseline.BaselineAnalysisCompletedEvent;
import miasi.backend.domains.analysis.simulation.MissionFailureDetectedEvent;
import miasi.backend.domains.analysis.simulation.SimulationAnalysisCompletedEvent;

public interface IAnalysisEventPublisherPort {
  void publishBaselineAnalysisCompleted(BaselineAnalysisCompletedEvent event);

  void publishSimulationAnalysisCompleted(SimulationAnalysisCompletedEvent event);

  void publishMissionFailureDetected(MissionFailureDetectedEvent event);
}
