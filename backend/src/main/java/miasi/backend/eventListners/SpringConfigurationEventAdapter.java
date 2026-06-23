package miasi.backend.eventListners;

import lombok.RequiredArgsConstructor;
import miasi.backend.domains.configuration.missionPlan.MissionPlan;
import miasi.backend.domains.configuration.ports.IConfigurationEventPublisherPort;
import miasi.backend.events.MissionPlanCreatedEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SpringConfigurationEventAdapter implements IConfigurationEventPublisherPort {

  private final ApplicationEventPublisher applicationEventPublisher;

  @Override
  public void publishMissionPlanCreated(int missionPlanId, MissionPlan plan) {
    applicationEventPublisher.publishEvent(new MissionPlanCreatedEvent(missionPlanId, plan));
  }
}
