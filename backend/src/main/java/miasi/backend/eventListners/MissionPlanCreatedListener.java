package miasi.backend.eventListners;

import miasi.backend.domains.schedule.MissionPlanEventInbox;
import miasi.backend.events.MissionPlanCreatedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class MissionPlanCreatedListener implements ApplicationListener<MissionPlanCreatedEvent> {
  private final MissionPlanEventInbox inbox;

  @Autowired
  public MissionPlanCreatedListener(MissionPlanEventInbox inbox) {
    this.inbox = inbox;
  }

  @Override
  public void onApplicationEvent(MissionPlanCreatedEvent event) {
    inbox.record(event);
  }
}
