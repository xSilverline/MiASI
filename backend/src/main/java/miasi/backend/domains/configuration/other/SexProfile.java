package miasi.backend.domains.configuration.other;

import lombok.*;
import lombok.experimental.FieldDefaults;
import miasi.backend.enums.ResourceType;

import java.util.Map;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor
public class SexProfile {
  String name;
  int population;
  Map<ResourceType, Float> optimalDemand;
  Map<ResourceType, Float> minimalDemand;

  //demands maps should use ResourceType.getDemandResourcesTypes() to get their keys

  void changePopulation(int amount) {

  }
}
