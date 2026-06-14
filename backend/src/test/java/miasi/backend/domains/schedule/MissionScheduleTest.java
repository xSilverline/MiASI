package miasi.backend.domains.schedule;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import miasi.backend.enums.DifficultyLevel;
import miasi.backend.enums.EventType;
import miasi.backend.enums.ScheduleStatus;
import miasi.backend.enums.ScenarioGenerationMode;
import org.junit.jupiter.api.Test;

class MissionScheduleTest {

  @Test
  void createDraft_shouldInitializeDraftSchedule() {
    MissionSchedule schedule = MissionSchedule.createDraft("plan-1", 120);

    assertNotNull(schedule.getId());
    assertEquals("plan-1", schedule.getMissionPlanId());
    assertEquals(120, schedule.getDurationSols());
    assertEquals(ScheduleStatus.DRAFT, schedule.getStatus());
    assertNotNull(schedule.getEvents());
    assertTrue(schedule.getEvents().isEmpty());
  }

  @Test
  void addEvent_shouldAppendEvent() {
    MissionSchedule schedule = MissionSchedule.createDraft("plan-1", 120);
    ScheduledEvent event = event("event-1", EventType.THREAT, 7);

    schedule.addEvent(event);

    assertEquals(List.of(event), schedule.getEvents());
  }

  @Test
  void updateEvent_shouldReplaceEventWithMatchingId() {
    MissionSchedule schedule =
        new MissionSchedule(
            "schedule-1",
            "plan-1",
            120,
            ScheduleStatus.DRAFT,
            new ArrayList<>(List.of(event("event-1", EventType.THREAT, 7))));
    ScheduledEvent updated = event("event-1", EventType.SUPPLY_DELIVERY, 9);

    schedule.updateEvent("event-1", updated);

    assertEquals(List.of(updated), schedule.getEvents());
  }

  @Test
  void removeEvent_shouldRemoveEventWithMatchingId() {
    ScheduledEvent first = event("event-1", EventType.THREAT, 7);
    ScheduledEvent second = event("event-2", EventType.SUPPLY_DELIVERY, 9);
    MissionSchedule schedule =
        new MissionSchedule(
            "schedule-1",
            "plan-1",
            120,
            ScheduleStatus.DRAFT,
            new ArrayList<>(List.of(first, second)));

    schedule.removeEvent("event-1");

    assertEquals(List.of(second), schedule.getEvents());
  }

  @Test
  void timeline_shouldReturnEventsSortedBySol() {
    ScheduledEvent third = event("event-3", EventType.MODULE_STATE_CHANGE, 30);
    ScheduledEvent first = event("event-1", EventType.THREAT, 1);
    ScheduledEvent second = event("event-2", EventType.SUPPLY_DELIVERY, 14);
    MissionSchedule schedule =
        new MissionSchedule(
            "schedule-1",
            "plan-1",
            120,
            ScheduleStatus.DRAFT,
            new ArrayList<>(List.of(third, first, second)));

    MissionTimeline timeline = schedule.timeline();

    assertEquals(List.of(first, second, third), timeline.getEventsSortedBySol());
  }

  @Test
  void updateEvent_shouldThrowWhenEventDoesNotExist() {
    MissionSchedule schedule = MissionSchedule.createDraft("plan-1", 120);
    ScheduledEvent event = event("event-1", EventType.THREAT, 7);

    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class, () -> schedule.updateEvent("missing-event", event));

    assertTrue(exception.getMessage().contains("missing-event"));
  }

  @Test
  void approveScenario_shouldReplaceEventsAndMarkScheduleReadyForAnalysis() {
    MissionSchedule schedule = MissionSchedule.createDraft("plan-1", 120);
    schedule.addEvent(event("old-event", EventType.SUPPLY_DELIVERY, 1));
    ScheduledEvent approvedEvent = event("event-1", EventType.THREAT, 7);
    ScenarioDraft draft =
        new ScenarioDraft(
            "draft-1",
            "plan-1",
            120,
            ScenarioGenerationMode.AUTOMATIC,
            DifficultyLevel.LEVEL_II,
            List.of(approvedEvent));

    schedule.approveScenario(draft);

    assertEquals(ScheduleStatus.READY_FOR_ANALYSIS, schedule.getStatus());
    assertEquals(List.of(approvedEvent), schedule.getEvents());
  }

  private ScheduledEvent event(String id, EventType type, int sol) {
    return new ScheduledEvent(id, type, sol, "description");
  }
}
