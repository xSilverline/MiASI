package miasi.backend.domains.analysis.domain.modules;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import miasi.backend.domains.analysis.domain.core.ResourceType;
import miasi.backend.domains.analysis.domain.core.Resource;

public class ProductionCalculator {

  public List<Resource> calculateModulesProduction(List<Module> currentModules) {
    Map<ResourceType, Float> dailyProduction = new EnumMap<>(ResourceType.class);
    for (Module module : currentModules) {

      if (module.getStatus() == ModuleState.ACTIVE) {

        for (Resource res : module.getProduction()) {
          float actualAmount = res.getAmount() * module.getEfficiency();

          dailyProduction.merge(res.getType(), actualAmount, Float::sum);
        }
      }
    }
    return dailyProduction.entrySet().stream()
        .map(entry -> new Resource(entry.getKey(), entry.getValue()))
        .collect(Collectors.toList());
  }
}