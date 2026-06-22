package miasi.backend.domains.analysis.types.crew;

import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import miasi.backend.domains.analysis.types.core.Resource;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
public class CrewGroup {
  String name;
  int count;
  ConsumptionProfile minimalNeeds;
  ConsumptionProfile optimalNeeds;

  public List<Resource> getDailyDemand(ConsumptionMode mode) {
    // Zwraca potrzeby profilu na podstawie trybu
    return mode == ConsumptionMode.OPTIMAL
        ? optimalNeeds.getDailyConsumption()
        : minimalNeeds.getDailyConsumption();
  }
}
