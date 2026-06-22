package miasi.backend.configuration.application.port.in;

import java.util.Optional;
import miasi.backend.configuration.domain.model.MissionPlan;

public interface GetMissionPlanUseCase {
  int getPlansCount();

  MissionPlan getDefaultMissionPlan();

  Optional<MissionPlan> getMissionPlan(int missionId);
}
