package miasi.backend.domains.analysis.domain.crew;

import java.util.List;
import lombok.Value;
import miasi.backend.domains.analysis.domain.core.Resource;

@Value
public class CrewGroup {

  String name;
  int count;
  ConsumptionProfile minimalNeeds;
  ConsumptionProfile optimalNeeds;

  public List<Resource> getDailyDemand(ConsumptionMode mode) {
    return mode == ConsumptionMode.OPTIMAL ?
        optimalNeeds.getDailyConsumption() :
        minimalNeeds.getDailyConsumption();
  }
}