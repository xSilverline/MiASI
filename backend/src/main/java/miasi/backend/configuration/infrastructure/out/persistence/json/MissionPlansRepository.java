package miasi.backend.configuration.infrastructure.out.persistence.json;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import miasi.backend.common.infrastructure.out.persistence.json.JsonFileStorage;
import miasi.backend.configuration.application.port.out.MissionPlanRepositoryPort;
import miasi.backend.configuration.domain.model.MissionPlan;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

@Repository
public class MissionPlansRepository implements MissionPlanRepositoryPort {
  private List<MissionPlan> plans = new ArrayList<>();
  private final String filePath;

  JsonFileStorage<MissionPlan> database = new JsonFileStorage<>(MissionPlan.class);

  public MissionPlansRepository(@Value("${database.filename.missions}") String filePath) {
    List<MissionPlan> plansTemp = database.loadListFromFile(filePath);
    if (plansTemp != null) {
      plans = plansTemp;
    }
    this.filePath = filePath;
  }

  @Override
  public Optional<MissionPlan> findById(int missionId) {
    if (missionId < 0 || missionId >= plans.size()) {
      return Optional.empty();
    }

    return Optional.of(plans.get(missionId));
  }

  @Override
  public int save(MissionPlan plan) {
    plans.add(plan);
    database.saveListToFile(plans, filePath);
    return plans.size() - 1;
  }

  @Override
  public int replace(int id, MissionPlan plan) {
    if (id >= 0 && id < getPlansCount()) {
      plans.set(id, plan);
      database.saveListToFile(plans, filePath);
      return id;
    } else {
      return -1;
    }
  }

  @Override
  public void delete(int missionId) {
    if (missionId < 0 || missionId >= plans.size()) {
      return;
    }

    plans.remove(missionId);
    database.saveListToFile(plans, filePath);
  }

  @Override
  public int getPlansCount() {
    return plans.size();
  }
}
