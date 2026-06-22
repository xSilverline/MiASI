package miasi.backend.eventListeners;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

import miasi.backend.configuration.application.ConfigurationApplicationService;
import miasi.backend.domains.configuration.missionPlan.MissionPlan;
import miasi.backend.domains.schedule.MissionPlanEventInbox;
import miasi.backend.events.MissionPlanCreated;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

@SpringBootTest
@ActiveProfiles("test")
class MissionPlanCreatedListenerTest {

  @Autowired private ConfigurationApplicationService configurationApplicationService;

  @Autowired private MissionPlanEventInbox inbox;

  @MockitoSpyBean private MissionPlanCreatedListener listener;

  @BeforeEach
  void clearInbox() {
    inbox.clear();
  }

  @Test
  void onMissionPlanCreated_shouldBeTriggered() {
    MissionPlan plan = new MissionPlan();

    int missionPlanId = configurationApplicationService.saveMissionPlan(plan);

    verify(listener)
        .onMissionPlanCreated(org.mockito.ArgumentMatchers.any(MissionPlanCreated.class));
    assertEquals(1, inbox.getMissionPlanCreatedEvents().size());
    assertEquals(missionPlanId, inbox.getMissionPlanCreatedEvents().getFirst().missionPlanId());
  }

  @Test
  void onMissionPlanUpdated_shouldBeTriggered() {
    MissionPlan plan = new MissionPlan();

    int missionPlanId = configurationApplicationService.overrideMissionPlan(0, plan).orElseThrow();

    verify(listener)
        .onMissionPlanUpdated(
            org.mockito.ArgumentMatchers.any(miasi.backend.events.MissionPlanUpdated.class));
    assertEquals(1, inbox.getMissionPlanUpdatedEvents().size());
    assertEquals(missionPlanId, inbox.getMissionPlanUpdatedEvents().getFirst().missionPlanId());
  }
}
