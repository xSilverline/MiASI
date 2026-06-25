package miasi.backend.eventListners;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

import miasi.backend.api.config.ConfService;
import miasi.backend.domains.configuration.missionPlan.MissionPlan;
import miasi.backend.domains.schedule.MissionPlanEventInbox;
import miasi.backend.events.MissionPlanCreatedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

@SpringBootTest
@ActiveProfiles("test")
class MissionPlanCreatedListenerTest {

  @Autowired private ConfService confService;

  @Autowired private MissionPlanEventInbox inbox;

  @MockitoSpyBean private MissionPlanCreatedListener listener;

  @BeforeEach
  void clearInbox() {
    inbox.clear();
  }

  @Test
  void onApplicationEvent_shouldBeTriggered() {
    MissionPlan plan = new MissionPlan();

    int missionPlanId = confService.saveMissionPlan(plan);

    verify(listener)
        .onApplicationEvent(org.mockito.ArgumentMatchers.any(MissionPlanCreatedEvent.class));
    assertEquals(1, inbox.getMissionPlanCreatedEvents().size());
    assertEquals(missionPlanId, inbox.getMissionPlanCreatedEvents().getFirst().getMissionPlanId());
  }
}
