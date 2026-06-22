package miasi.backend.eventListeners;

import lombok.RequiredArgsConstructor;
import miasi.backend.analysis.application.port.out.AnalysisEventPublisherPort;
import miasi.backend.domains.analysis.baseline.BaselineAnalysisCompletedEvent;
import miasi.backend.domains.analysis.simulation.MissionFailureDetectedEvent;
import miasi.backend.domains.analysis.simulation.SimulationAnalysisCompletedEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SpringAnalysisEventAdapter implements AnalysisEventPublisherPort {
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
