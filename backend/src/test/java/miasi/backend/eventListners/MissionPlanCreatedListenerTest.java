package miasi.backend.eventListners;

import miasi.backend.domains.configuration.ConfService;
import miasi.backend.domains.configuration.missionPlan.MissionPlan;
import miasi.backend.events.MissionPlanCreatedEvent;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import static org.mockito.Mockito.verify;

@SpringBootTest
@ActiveProfiles("test")
class MissionPlanCreatedListenerTest {
  
  @Autowired
  private ConfService confService;

  @MockitoSpyBean
  private MissionPlanCreatedListener listener;

  @Test
  void onApplicationEvent_shouldBeTriggered() {
    MissionPlan plan = new MissionPlan();

    confService.saveMissionPlan(plan);

    verify(listener).onApplicationEvent(
        org.mockito.ArgumentMatchers.any(MissionPlanCreatedEvent.class)
    );
  }
}