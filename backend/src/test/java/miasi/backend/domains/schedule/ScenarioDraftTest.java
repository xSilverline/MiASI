package miasi.backend.domains.schedule;

import miasi.backend.domains.schedule.enums.DifficultyLevel;
import miasi.backend.domains.schedule.enums.EventType;
import miasi.backend.domains.schedule.enums.ScenarioGenerationMode;
import miasi.backend.domains.schedule.enums.ScheduleStatus;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ScenarioDraftTest {

  @Test
  void correctEvent_shouldReplaceEventWithMatchingId() {
    // Given
    ScheduledEvent original = event("event-1", EventType.THREAT, 3);
    ScheduledEvent second = event("event-2", EventType.SUPPLY_DELIVERY, 5);
    ScheduledEvent corrected = event("event-1", EventType.MODULE_STATE_CHANGE, 8);
    ScenarioDraft draft = draftWithEvents(new ArrayList<>(List.of(original, second)));

    // When
    draft.correctEvent("event-1", corrected);

    // Then
    assertEquals(List.of(corrected, second), draft.getProposedEvents());
  }

  @Test
  void correctEvent_shouldThrowWhenEventDoesNotExist() {
    // Given
    ScenarioDraft draft =
        draftWithEvents(new ArrayList<>(List.of(event("event-1", EventType.THREAT, 3))));

    // When + Then
    assertThrows(
        IllegalArgumentException.class,
        () -> draft.correctEvent("missing-event", event("event-2", EventType.THREAT, 4)));
  }

  @Test
  void approve_shouldCreateReadyScheduleWithDraftEvents() {
    // Given
    ScheduledEvent event = event("event-1", EventType.THREAT, 3);
    ScenarioDraft draft = draftWithEvents(new ArrayList<>(List.of(event)));

    // When
    MissionSchedule schedule = draft.approve();

    // Then
    assertEquals("plan-1", schedule.getMissionPlanId());
    assertEquals(120, schedule.getDurationSols());
    assertEquals(ScheduleStatus.READY_FOR_ANALYSIS, schedule.getStatus());
    assertEquals(List.of(event), schedule.getEvents());
  }

  @Test
  void approve_exceptionsThrowTest() {
    // Given
    ScenarioDraft draft = draftWithEvents(new ArrayList<>(List.of()));

    // When + Then (Valid data)
    assertDoesNotThrow(() -> {
      draft.approve();
    });

    // When + Then (MissionPlanId is blank)
    draft.setMissionPlanId("");
    assertThrows(IllegalArgumentException.class, draft::approve);

    // When + Then (DurationSols is 0)
    draft.setMissionPlanId("Adam");
    draft.setDurationSols(0);
    assertThrows(IllegalArgumentException.class, draft::approve);
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
