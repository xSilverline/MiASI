package miasi.backend.domains.analysis.infrastructure.out.events;

import lombok.extern.slf4j.Slf4j;
import miasi.backend.domains.analysis.application.port.out.IAnalysisEventPublisherPort;
import miasi.backend.domains.analysis.domain._payload.PayloadOptimizationCompletedEvent;
import miasi.backend.domains.analysis.domain._simulation.NominalSimulationCompletedEvent;
import miasi.backend.domains.analysis.domain._simulation.ScenariosAnalysisCompletedEvent;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Primary
@Slf4j
@Component
public class AnalysisEventPublisherAdapter implements IAnalysisEventPublisherPort {

  @Override
  public void publishPayloadOptimizationCompleted(PayloadOptimizationCompletedEvent event) {
  }

  @Override
  public void publishNominalSimulationCompleted(NominalSimulationCompletedEvent event) {
  }

  @Override
  public void publishScenariosAnalysisCompleted(ScenariosAnalysisCompletedEvent event) {

  }
}