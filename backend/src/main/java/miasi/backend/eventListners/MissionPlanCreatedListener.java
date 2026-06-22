package miasi.backend.eventListners;

import miasi.backend.domains.configuration.ConfService;
import miasi.backend.events.MissionPlanCreatedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class MissionPlanCreatedListener implements ApplicationListener<MissionPlanCreatedEvent> {
  private final ConfService confService; // słuchacz rozmawia z serwisem a nie z bazą bezpośrednio

  @Autowired
  public MissionPlanCreatedListener(ConfService confService) {
    this.confService = confService;
  }

  @Override
  public void onApplicationEvent(MissionPlanCreatedEvent event) {
    //TODO: obsługa wydarzenia
  }
}
