package miasi.backend.configuration.infrastructure.out.event;

import lombok.RequiredArgsConstructor;
import miasi.backend.common.domain.model.event.MissionPlanCreated;
import miasi.backend.common.domain.model.event.MissionPlanUpdated;
import miasi.backend.configuration.application.port.out.ConfigurationEventPublisherPort;
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
