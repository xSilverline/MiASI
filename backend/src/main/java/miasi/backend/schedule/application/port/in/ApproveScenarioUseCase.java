package miasi.backend.schedule.application.port.in;

import miasi.backend.schedule.domain.model.MissionSchedule;

public interface ApproveScenarioUseCase {
  MissionSchedule approveScenarioDraft(String draftId);

  MissionSchedule approveScenarioIntoSchedule(String scheduleId, String draftId);
}
