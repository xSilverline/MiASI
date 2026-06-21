package miasi.backend.domains.schedule;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import miasi.backend.enums.DifficultyLevel;
import miasi.backend.enums.EventType;
import miasi.backend.enums.ModuleState;
import miasi.backend.enums.ScheduleStatus;
import org.junit.jupiter.api.Test;

class ScheduleServiceTest {

  @Test
  void shouldCreateScheduleAndManageEvents() {
    ScheduleService service = new ScheduleService();
    MissionSchedule schedule = service.createSchedule("plan-1", 120);
    ScheduledEvent event = new ScheduledEvent("event-1", EventType.THREAT, 7, "description");

    service.addEvent(schedule.getId(), event);

    assertEquals(schedule.getId(), service.getSchedule(schedule.getId()).getId());
    assertEquals(1, service.getTimeline(schedule.getId()).getEventsSortedBySol().size());

    service.removeEvent(schedule.getId(), "event-1");

    assertEquals(0, service.getSchedule(schedule.getId()).getEvents().size());
  }

  @Test
  void shouldAllowMultipleEventsInSameSol() {
    ScheduleService service = new ScheduleService();
    MissionSchedule schedule = service.createSchedule("plan-1", 120);

    service.addEvent(schedule.getId(), new ScheduledEvent("event-1", EventType.THREAT, 7, "dust"));
    service.addEvent(
        schedule.getId(), new ScheduledEvent("event-2", EventType.SUPPLY_DELIVERY, 7, "supply"));

    assertEquals(2, service.getSchedule(schedule.getId()).getEvents().size());
  }

  @Test
  void shouldScheduleModuleStateChange() {
    ScheduleService service = new ScheduleService();
    MissionSchedule schedule = service.createSchedule("plan-1", 120);

    MissionSchedule updated =
        service.scheduleModuleStateChange(
            schedule.getId(),
            null,
            12,
            "Habitat module partially damaged",
            "habitat-1",
            ModuleState.PARTIALLY_DAMAGED);

    ModuleStateChange stateChange =
        assertInstanceOf(ModuleStateChange.class, updated.getEvents().get(0));
    assertNotNull(stateChange.getId());
    assertEquals(EventType.MODULE_STATE_CHANGE, stateChange.getType());
    assertEquals(12, stateChange.getSol());
    assertEquals("habitat-1", stateChange.getModuleId());
    assertEquals(ModuleState.PARTIALLY_DAMAGED, stateChange.getNewState());
  }

  @Test
  void shouldGenerateAndApproveScenarioDraft() {
    ScheduleService service = new ScheduleService();

    ScenarioDraft draft = service.generateScenario("plan-1", 90, DifficultyLevel.LEVEL_II);
    MissionSchedule schedule = service.approveScenarioDraft(draft.getId());

    assertFalse(draft.getProposedEvents().isEmpty());
    assertEquals(ScheduleStatus.READY_FOR_ANALYSIS, schedule.getStatus());
    assertEquals(draft.getProposedEvents(), schedule.getEvents());
  }
}
