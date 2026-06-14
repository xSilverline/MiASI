package miasi.backend.domains.schedule;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;
import miasi.backend.enums.DifficultyLevel;
import miasi.backend.enums.EventType;
import miasi.backend.enums.ScheduleStatus;
import miasi.backend.enums.ScenarioGenerationMode;
import org.junit.jupiter.api.Test;

class ScenarioDraftTest {

  @Test
  void correctEvent_shouldReplaceEventWithMatchingId() {
    ScheduledEvent original = event("event-1", EventType.THREAT, 3);
    ScheduledEvent second = event("event-2", EventType.SUPPLY_DELIVERY, 5);
    ScheduledEvent corrected = event("event-1", EventType.MODULE_STATE_CHANGE, 8);
    ScenarioDraft draft = draftWithEvents(new ArrayList<>(List.of(original, second)));

    draft.correctEvent("event-1", corrected);

    assertEquals(List.of(corrected, second), draft.getProposedEvents());
  }

  @Test
  void correctEvent_shouldThrowWhenEventDoesNotExist() {
    ScenarioDraft draft =
        draftWithEvents(new ArrayList<>(List.of(event("event-1", EventType.THREAT, 3))));

    assertThrows(
        IllegalArgumentException.class,
        () -> draft.correctEvent("missing-event", event("event-2", EventType.THREAT, 4)));
  }

  @Test
  void approve_shouldCreateReadyScheduleWithDraftEvents() {
    ScheduledEvent event = event("event-1", EventType.THREAT, 3);
    ScenarioDraft draft = draftWithEvents(new ArrayList<>(List.of(event)));

    MissionSchedule schedule = draft.approve();

    assertEquals("plan-1", schedule.getMissionPlanId());
    assertEquals(120, schedule.getDurationSols());
    assertEquals(ScheduleStatus.READY_FOR_ANALYSIS, schedule.getStatus());
    assertEquals(List.of(event), schedule.getEvents());
  }

  private ScenarioDraft draftWithEvents(List<ScheduledEvent> events) {
    return new ScenarioDraft(
        "draft-1",
        "plan-1",
        120,
        ScenarioGenerationMode.AUTOMATIC,
        DifficultyLevel.LEVEL_II,
        events);
  }

  private ScheduledEvent event(String id, EventType type, int sol) {
    return new ScheduledEvent(id, type, sol, "description");
  }
}
