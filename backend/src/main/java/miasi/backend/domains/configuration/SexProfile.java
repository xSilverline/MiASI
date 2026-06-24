package miasi.backend.domains.configuration;

import java.util.HashMap;
import java.util.Map;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import miasi.backend.domains.configuration.enums.ResourceType;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
public class SexProfile {
  String name;
  int population;
  Map<ResourceType, Float> optimalDemand;
  Map<ResourceType, Float> minimalDemand;

  public SexProfile() {
    name = "default";
    population = 0;
    optimalDemand = new HashMap<>();
    minimalDemand = new HashMap<>();
    ResourceType[] types = ResourceType.getDemandResourcesTypes();
    for (ResourceType type : types) {
      optimalDemand.put(type, 0f);
      minimalDemand.put(type, 0f);
    }
  }

  public void changePopulation(int amount) {
    population += amount;
  }
}
