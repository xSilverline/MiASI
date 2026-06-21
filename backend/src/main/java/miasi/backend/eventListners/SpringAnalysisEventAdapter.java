package miasi.backend.eventListners;

import lombok.RequiredArgsConstructor;
import miasi.backend.domains.analisis.baseline.BaselineAnalysisCompletedEvent;
import miasi.backend.domains.analisis.ports.IAnalysisEventPublisherPort;
import miasi.backend.domains.analisis.simulation.MissionFailureDetectedEvent;
import miasi.backend.domains.analisis.simulation.SimulationAnalysisCompletedEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SpringAnalysisEventAdapter implements IAnalysisEventPublisherPort {
  private final ApplicationEventPublisher applicationEventPublisher;

  @Override
  public void publishBaselineAnalysisCompleted(BaselineAnalysisCompletedEvent event) {
    applicationEventPublisher.publishEvent(event);
  }

  @Override
  public void publishSimulationAnalysisCompleted(SimulationAnalysisCompletedEvent event) {
    applicationEventPublisher.publishEvent(event);
  }

  @Override
  public void publishMissionFailureDetected(MissionFailureDetectedEvent event) {
    applicationEventPublisher.publishEvent(event);
  }
}
