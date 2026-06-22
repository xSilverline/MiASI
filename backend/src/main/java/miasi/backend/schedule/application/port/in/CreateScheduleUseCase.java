package miasi.backend.schedule.application.port.in;

import miasi.backend.domains.schedule.MissionSchedule;

public interface CreateScheduleUseCase {
  MissionSchedule createSchedule(String missionPlanId, int durationSols);
}
