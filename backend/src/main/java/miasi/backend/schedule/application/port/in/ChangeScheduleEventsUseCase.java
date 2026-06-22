package miasi.backend.schedule.application.port.in;

import miasi.backend.common.domain.model.ModuleState;
import miasi.backend.schedule.domain.model.MissionSchedule;
import miasi.backend.schedule.domain.model.ScheduledEvent;

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
