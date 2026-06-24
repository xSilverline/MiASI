package miasi.backend.domains.analysis.domain._payload;

import java.util.List;
import lombok.RequiredArgsConstructor;
import miasi.backend.domains.analysis.domain.core.Resource;
import miasi.backend.domains.analysis.domain.modules.Module;

@RequiredArgsConstructor
public class WeightCalculator {

  public float calculateTotalWeight(List<Module> modules, List<Resource> resources) {
    // sum the weights of all modules (getWeight) and resources (using weightDictionary.calculateWeight)
    float totalWeight = 0.0f;
    // sum up the weight of all modules
    if (modules != null) {
      for (Module module : modules) {
        totalWeight += module.getWeight();
      }
    }

    // sum up the weight of all resources
    if (resources != null) {
      for (Resource resource : resources) {
        totalWeight += resource.getWeight();
      }
    }
    return totalWeight;
  }

  public boolean isLimitExceeded(float totalWeight, float maxWeight) {
    // return true if the total weight exceeds the limit defined in the manifest
    return totalWeight > maxWeight;
  }
}