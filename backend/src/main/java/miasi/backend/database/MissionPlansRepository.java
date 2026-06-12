package miasi.backend.database;

import miasi.backend.domains.configuration.missionPlan.MissionPlan;
import miasi.backend.domains.configuration.ports.IMissionPlanRepositoryPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import tools.jackson.core.type.TypeReference;

import java.util.ArrayList;
import java.util.List;

@Repository
public class MissionPlansRepository implements IMissionPlanRepositoryPort {
  private List<MissionPlan> plans = new ArrayList<>();
  private final String filePath;

  JsonFileStorage database = new JsonFileStorage();

  public MissionPlansRepository(
      @Value("${database.filename.missions}") String filePath
  ) {
    List<MissionPlan> plansTemp =
        database.loadFromFile(
            filePath,
            new TypeReference<>() {
            }
        );
    if (plansTemp != null)
      plans = plansTemp;
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
    database.saveToFile(plans, filePath);
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
