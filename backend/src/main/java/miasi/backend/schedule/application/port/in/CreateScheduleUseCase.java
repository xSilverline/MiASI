package miasi.backend.schedule.application.port.in;

import miasi.backend.schedule.domain.model.MissionSchedule;

public interface CreateScheduleUseCase {
  MissionSchedule createSchedule(String missionPlanId, int durationSols);
}
