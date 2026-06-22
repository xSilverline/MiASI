package miasi.backend.schedule.application.port.in;

import miasi.backend.schedule.domain.model.MissionSchedule;

public interface GetScheduleUseCase {
  MissionSchedule getSchedule(String scheduleId);
}
