package miasi.backend.schedule.application.port.in;

import miasi.backend.domains.schedule.MissionSchedule;
import miasi.backend.domains.schedule.ScheduledEvent;
import miasi.backend.sharedkernel.model.ModuleState;

public interface ChangeScheduleEventsUseCase {
  MissionSchedule addEvent(String scheduleId, ScheduledEvent event);

  MissionSchedule updateEvent(String scheduleId, String eventId, ScheduledEvent event);

  MissionSchedule scheduleModuleStateChange(
      String scheduleId,
      String eventId,
      int sol,
      String description,
      String moduleId,
      ModuleState newState);

  void removeEvent(String scheduleId, String eventId);
}
