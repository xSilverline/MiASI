package miasi.backend.domains.configuration.missionPlan;

import miasi.backend.database.JsonFileStorage;
import miasi.backend.events.MissionPlanCreatedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class MissionPlansRepository {
  private List<MissionPlan> plans = new ArrayList<>();
  private final String filePath;

  JsonFileStorage<List<MissionPlan>> database = new JsonFileStorage<>();

  @Autowired
  private ApplicationEventPublisher applicationEventPublisher;

  public MissionPlansRepository(
      @Value("${database.filename.missions}") String filePath
  ) {
    List<MissionPlan> plansTemp = database.loadFromFile(filePath);
    if (plansTemp != null)
      plans = plansTemp;
    this.filePath = filePath;
  }

  public MissionPlan findById(int missionId) {
    try {
      return plans.get(missionId);
    } catch (IndexOutOfBoundsException ex) {
      return null;
    }
  }

  public int save(MissionPlan plan) {
    plans.add(plan);
    database.saveToFile(plans, filePath);
    this.throwCreatedEvent();
    return plans.size() - 1;
  }

  public void delete(int missionId) {
    plans.remove(missionId);
  }

  public void throwCreatedEvent() {
    applicationEventPublisher.publishEvent(new MissionPlanCreatedEvent(plans.getLast()));
  }
}
