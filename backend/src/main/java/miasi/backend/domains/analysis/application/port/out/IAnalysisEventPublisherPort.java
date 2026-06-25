package miasi.backend.domains.analysis.application.port.out;

import miasi.backend.domains.analysis.domain._payload.PayloadOptimizationCompletedEvent;
import miasi.backend.domains.analysis.domain._simulation.NominalSimulationCompletedEvent;
import miasi.backend.domains.analysis.domain._simulation.ScenariosAnalysisCompletedEvent;

public interface IAnalysisEventPublisherPort {

  void publishPayloadOptimizationCompleted(PayloadOptimizationCompletedEvent event);

  void publishNominalSimulationCompleted(NominalSimulationCompletedEvent event);

  void publishScenariosAnalysisCompleted(ScenariosAnalysisCompletedEvent event);
}
