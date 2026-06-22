package miasi.backend.schedule.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.ArrayList;
import java.util.List;
import miasi.backend.domains.schedule.MissionSchedule;
import miasi.backend.domains.schedule.ModuleStateChange;
import miasi.backend.domains.schedule.ScenarioDraft;
import miasi.backend.domains.schedule.ScheduledEvent;
import miasi.backend.schedule.application.port.out.ScheduleEventPublisherPort;
import miasi.backend.schedule.domain.DifficultyLevel;
import miasi.backend.schedule.domain.EventType;
import miasi.backend.schedule.domain.ScheduleStatus;
import miasi.backend.sharedkernel.model.ModuleState;
import org.junit.jupiter.api.Test;

class ScheduleApplicationServiceTest {

  @Test
  void shouldCreateScheduleAndManageEvents() {
    // Given
    RecordingScheduleEventPublisher publisher = new RecordingScheduleEventPublisher();
    ScheduleApplicationService service = new ScheduleApplicationService(publisher);
    MissionSchedule schedule = service.createSchedule("plan-1", 120);
    ScheduledEvent event = new ScheduledEvent("event-1", EventType.THREAT, 7, "description");

    // When
    service.addEvent(schedule.getId(), event);

    // Then
    assertEquals(schedule.getId(), service.getSchedule(schedule.getId()).getId());
    assertEquals(1, service.getTimeline(schedule.getId()).getEventsSortedBySol().size());
    service.removeEvent(schedule.getId(), "event-1");
    assertEquals(0, service.getSchedule(schedule.getId()).getEvents().size());
    assertEquals(List.of("created", "event-added", "updated", "updated"), publisher.calls);
  }

  @Test
  void shouldAllowMultipleEventsInSameSol() {
    // Given
    ScheduleApplicationService service = new ScheduleApplicationService();
    MissionSchedule schedule = service.createSchedule("plan-1", 120);

    // When
    service.addEvent(schedule.getId(), new ScheduledEvent("event-1", EventType.THREAT, 7, "dust"));
    service.addEvent(
        schedule.getId(), new ScheduledEvent("event-2", EventType.SUPPLY_DELIVERY, 7, "supply"));

    // Then
    assertEquals(2, service.getSchedule(schedule.getId()).getEvents().size());
  }

  @Test
  void shouldScheduleModuleStateChange() {
    // Given
    ScheduleApplicationService service = new ScheduleApplicationService();
    MissionSchedule schedule = service.createSchedule("plan-1", 120);

    // When
    MissionSchedule updated =
        service.scheduleModuleStateChange(
            schedule.getId(),
            null,
            12,
            "Habitat module partially damaged",
            "habitat-1",
            ModuleState.PARTIALLY_DAMAGED);

    // Then
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
    // Given
    RecordingScheduleEventPublisher publisher = new RecordingScheduleEventPublisher();
    ScheduleApplicationService service = new ScheduleApplicationService(publisher);

    // When
    ScenarioDraft draft = service.generateScenario("plan-1", 90, DifficultyLevel.LEVEL_II);
    MissionSchedule schedule = service.approveScenarioDraft(draft.getId());

    // Then
    assertFalse(draft.getProposedEvents().isEmpty());
    assertEquals(ScheduleStatus.READY_FOR_ANALYSIS, schedule.getStatus());
    assertEquals(draft.getProposedEvents(), schedule.getEvents());
    assertEquals(List.of("created"), publisher.calls);
  }

  private static class RecordingScheduleEventPublisher implements ScheduleEventPublisherPort {
    private final List<String> calls = new ArrayList<>();

    @Override
    public void publishScheduleCreated(MissionSchedule schedule) {
      calls.add("created");
    }

    @Override
    public void publishScheduleUpdated(MissionSchedule schedule) {
      calls.add("updated");
    }

    @Override
    public void publishScheduledEventAdded(String scheduleId, ScheduledEvent event) {
      calls.add("event-added");
    }
  }
}
