package configuration.other;

import configuration.enums.ResourceType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.util.Map;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
public class SexProfile {
  final String name;
  int population;
  Map<ResourceType, Float> optimalDemand;
  Map<ResourceType, Float> minimalDemand;

  //demands maps should use ResourceType.getDemandResourcesTypes() to get their keys

  void changePopulation(int amount) {

  }
}
