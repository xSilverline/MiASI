package miasi.backend.eventListners;

import miasi.backend.domains.configuration.missionPlan.MissionPlansRepository;
import miasi.backend.events.MissionPlanCreatedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class MissionPlanCreatedListener implements ApplicationListener<MissionPlanCreatedEvent> {
  MissionPlansRepository repository;

  @Autowired
  public MissionPlanCreatedListener(MissionPlansRepository repository) {
    this.repository = repository;
  }

  @Override
  public void onApplicationEvent(MissionPlanCreatedEvent event) {
    //TODO: obsługa wydarzenia
  }
}
