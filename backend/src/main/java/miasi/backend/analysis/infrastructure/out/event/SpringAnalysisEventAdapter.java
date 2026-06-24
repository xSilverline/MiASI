package miasi.backend.analysis.infrastructure.out.event;

import lombok.RequiredArgsConstructor;
import miasi.backend.analysis.application.port.out.AnalysisEventPublisherPort;
import miasi.backend.analysis.domain.model.baseline.BaselineAnalysisCompletedEvent;
import miasi.backend.analysis.domain.model.simulation.MissionFailureDetectedEvent;
import miasi.backend.analysis.domain.model.simulation.SimulationAnalysisCompletedEvent;
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
