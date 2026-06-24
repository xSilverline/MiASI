package miasi.backend.domains.analysis.domain._payload;

import java.util.List;
import lombok.RequiredArgsConstructor;
import miasi.backend.domains.analysis.domain.core.Resource;
import miasi.backend.domains.analysis.domain.modules.Module;

@RequiredArgsConstructor
public class WeightCalculator {

  public float calculateTotalWeight(List<Module> modules, List<Resource> resources) {
    float totalWeight = 0.0f;
    if (modules != null) {
      for (Module module : modules) {
        totalWeight += module.getWeight();
      }
    }

    if (resources != null) {
      for (Resource resource : resources) {
        totalWeight += resource.getWeight();
      }
    }
    return totalWeight;
  }

  public boolean isLimitExceeded(float totalWeight, float maxWeight) {
    return totalWeight > maxWeight;
  }
}