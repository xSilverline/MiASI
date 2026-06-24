package miasi.backend.eventListners;

import lombok.RequiredArgsConstructor;
import miasi.backend.domains.analysis.application.port.out.IAnalysisEventPublisherPort;
import miasi.backend.domains.analysis.domain._payload.PayloadOptimizationCompletedEvent;
import miasi.backend.domains.analysis.domain._simulation.NominalSimulationCompletedEvent;
import miasi.backend.domains.analysis.domain._simulation.ScenariosAnalysisCompletedEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SpringAnalysisEventAdapter implements IAnalysisEventPublisherPort {
  private final ApplicationEventPublisher applicationEventPublisher;

  @Override
  public void publishPayloadOptimizationCompleted(PayloadOptimizationCompletedEvent event) {
    applicationEventPublisher.publishEvent(event);
  }

  @Override
  public void publishNominalSimulationCompleted(NominalSimulationCompletedEvent event) {
    applicationEventPublisher.publishEvent(event);
  }

  @Override
  public void publishScenariosAnalysisCompleted(ScenariosAnalysisCompletedEvent event) {
    applicationEventPublisher.publishEvent(event);
  }
}
