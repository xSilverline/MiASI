package miasi.backend.schedule.infrastructure.in.event;

import miasi.backend.common.domain.model.event.MissionPlanCreated;
import miasi.backend.common.domain.model.event.MissionPlanUpdated;
import miasi.backend.schedule.domain.model.MissionPlanEventInbox;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class MissionPlanCreatedListener {
  private final MissionPlanEventInbox inbox;

  @Autowired
  public MissionPlanCreatedListener(MissionPlanEventInbox inbox) {
    this.inbox = inbox;
  }

  @EventListener
  public void onMissionPlanCreated(MissionPlanCreated event) {
    inbox.record(event);
  }

  @EventListener
  public void onMissionPlanUpdated(MissionPlanUpdated event) {
    inbox.record(event);
  }
}
