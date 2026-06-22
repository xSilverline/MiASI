package miasi.backend.eventListeners;

import lombok.RequiredArgsConstructor;
import miasi.backend.configuration.application.port.out.ConfigurationEventPublisherPort;
import miasi.backend.events.MissionPlanCreated;
import miasi.backend.events.MissionPlanUpdated;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SpringConfigurationEventAdapter implements ConfigurationEventPublisherPort {

  private final ApplicationEventPublisher applicationEventPublisher;

  @Override
  public void publishMissionPlanCreated(int missionPlanId) {
    applicationEventPublisher.publishEvent(MissionPlanCreated.create(missionPlanId));
  }

  @Override
  public void publishMissionPlanUpdated(int missionPlanId) {
    applicationEventPublisher.publishEvent(MissionPlanUpdated.create(missionPlanId));
  }
}
