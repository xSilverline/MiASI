package miasi.backend.database;

import java.util.ArrayList;
import java.util.List;
import miasi.backend.domains.configuration.missionPlan.MissionPlan;
import miasi.backend.domains.configuration.ports.IMissionPlanRepositoryPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

@Repository
public class MissionPlansRepository implements IMissionPlanRepositoryPort {
  private List<MissionPlan> plans = new ArrayList<>();
  private final String filePath;

  JsonFileStorage<MissionPlan> database = new JsonFileStorage<>(MissionPlan.class);

  public MissionPlansRepository(@Value("${database.filename.missions}") String filePath) {
    List<MissionPlan> plansTemp = database.loadListFromFile(filePath);
    if (plansTemp != null) plans = plansTemp;
    this.filePath = filePath;
  }

  @Override
  public MissionPlan findById(int missionId) {
    try {
      return plans.get(missionId);
    } catch (IndexOutOfBoundsException ex) {
      return null;
    }
  }

  @Override
  public int save(MissionPlan plan) {
    plans.add(plan);
    database.saveListToFile(plans, filePath);
    return plans.size() - 1;
  }

  @Override
  public int replace(int id, MissionPlan plan) {
    if (id < getPlansCount()) {
      plans.set(id, plan);
      return id;
    } else {
      return -1;
    }
  }

  @Override
  public void delete(int missionId) {
    plans.remove(missionId);
  }

  @Override
  public int getPlansCount() {
    return plans.size();
  }
}
