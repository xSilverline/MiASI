package miasi.backend.configuration.application.port.in;

import java.util.Optional;
import miasi.backend.domains.configuration.missionPlan.MissionPlan;

public interface GetMissionPlanUseCase {
  int getPlansCount();

  MissionPlan getDefaultMissionPlan();

  Optional<MissionPlan> getMissionPlan(int missionId);
}
