package miasi.backend.schedule.application.port.in;

import miasi.backend.domains.schedule.MissionSchedule;

public interface GetScheduleUseCase {
  MissionSchedule getSchedule(String scheduleId);
}
