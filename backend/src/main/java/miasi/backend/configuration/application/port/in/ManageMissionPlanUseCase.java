package miasi.backend.configuration.application.port.in;

import java.util.OptionalInt;
import miasi.backend.domains.configuration.missionPlan.MissionPlan;

public interface ManageMissionPlanUseCase {
  int saveMissionPlan(MissionPlan missionPlan);

  OptionalInt overrideMissionPlan(int id, MissionPlan missionPlan);
}
