package miasi.backend.schedule.application.port.in;

import miasi.backend.domains.schedule.MissionSchedule;

public interface ApproveScenarioUseCase {
  MissionSchedule approveScenarioDraft(String draftId);

  MissionSchedule approveScenarioIntoSchedule(String scheduleId, String draftId);
}
