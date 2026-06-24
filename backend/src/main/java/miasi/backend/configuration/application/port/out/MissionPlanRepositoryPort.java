package miasi.backend.configuration.application.port.out;

import java.util.Optional;
import miasi.backend.configuration.domain.model.MissionPlan;

public interface MissionPlanRepositoryPort {
  Optional<MissionPlan> findById(int missionId);

  int save(MissionPlan plan);

  int replace(int id, MissionPlan plan);

  void delete(int missionId);

  int getPlansCount();
}
