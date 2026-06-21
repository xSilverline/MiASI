package miasi.backend.domains.schedule;

import miasi.backend.enums.DifficultyLevel;
import miasi.backend.enums.EventType;
import miasi.backend.enums.ScheduleStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class ScheduleServiceTest {

  @Test
  void shouldCreateScheduleAndManageEvents() {
    // Given
    ScheduleService service = new ScheduleService();
    MissionSchedule schedule = service.createSchedule("plan-1", 120);
    ScheduledEvent event = new ScheduledEvent("event-1", EventType.THREAT, 7, "description");

    // When
    service.addEvent(schedule.getId(), event);

    // Then
    assertEquals(schedule.getId(), service.getSchedule(schedule.getId()).getId());
    assertEquals(1, service.getTimeline(schedule.getId()).getEventsSortedBySol().size());
    service.removeEvent(schedule.getId(), "event-1");
    assertEquals(0, service.getSchedule(schedule.getId()).getEvents().size());
  }

  @Test
  void shouldGenerateAndApproveScenarioDraft() {
    // Given
    ScheduleService service = new ScheduleService();

    // When
    ScenarioDraft draft = service.generateScenario("plan-1", 90, DifficultyLevel.LEVEL_II);
    MissionSchedule schedule = service.approveScenarioDraft(draft.getId());

    // Then
    assertFalse(draft.getProposedEvents().isEmpty());
    assertEquals(ScheduleStatus.READY_FOR_ANALYSIS, schedule.getStatus());
    assertEquals(draft.getProposedEvents(), schedule.getEvents());
  }
}
