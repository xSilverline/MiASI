package miasi.backend.domains.configuration.missionPlan;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;
import org.springframework.web.context.WebApplicationContext;

import java.util.ArrayList;
import java.util.List;

@Repository
@Scope(WebApplicationContext.SCOPE_APPLICATION)
public class MissionPlansRepository {
  private final List<MissionPlan> plans = new ArrayList<>();

  public MissionPlansRepository() {
    //TODO: zczytywanie z bazy
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
    return plans.size() - 1;
  }

  public void delete(int missionId) {
    plans.remove(missionId);
  }
}
