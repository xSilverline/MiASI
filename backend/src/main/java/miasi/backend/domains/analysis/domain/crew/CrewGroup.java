package miasi.backend.domains.analysis.domain.crew;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import miasi.backend.domains.analysis.domain.core.Resource;

@Value
@Builder
@AllArgsConstructor
public class CrewGroup {

  String name;
  int count;
  ConsumptionProfile minimalNeeds;
  ConsumptionProfile optimalNeeds;

  public List<Resource> getDailyDemand(ConsumptionMode mode) {
    return mode == ConsumptionMode.OPTIMAL
        ? optimalNeeds.getDailyConsumption()
        : minimalNeeds.getDailyConsumption();
  }
}
