package miasi.backend.domains.analysis.domain.crew;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import miasi.backend.domains.analysis.domain.core.Resource;
import miasi.backend.domains.analysis.domain.core.ResourceType;
import miasi.backend.domains.analysis.domain.modules.Module;
import miasi.backend.domains.analysis.domain.modules.ModuleState;

public class DemandCalculator {

  public List<Resource> calculateCrewDemand(List<CrewGroup> crewGroups, ConsumptionMode mode) {
    Map<ResourceType, Float> dailyCrewDemand = new EnumMap<>(ResourceType.class);

    for (CrewGroup group : crewGroups) {

      List<Resource> perPersonDemand = group.getDailyDemand(mode);

      int peopleCount = group.getCount();

      for (Resource res : perPersonDemand) {
        float totalAmount = res.getAmount() * peopleCount;

        dailyCrewDemand.merge(res.getType(), totalAmount, Float::sum);
      }
    }

    return dailyCrewDemand.entrySet().stream()
        .map(entry -> new Resource(entry.getKey(), entry.getValue()))
        .collect(Collectors.toList());
  }

  public List<Resource> calculateModulesDemand(List<Module> currentModules) {
    Map<ResourceType, Float> dailyModulesDemand = new EnumMap<>(ResourceType.class);

    for (Module module : currentModules) {
      if (module.getStatus() == ModuleState.ACTIVE) {
        for (Resource res : module.getConsumption()) {
          float actualAmount = res.getAmount() * module.getEfficiency();
          dailyModulesDemand.merge(res.getType(), actualAmount, Float::sum);
        }
      }
    }

    return dailyModulesDemand.entrySet().stream()
        .map(entry -> new Resource(entry.getKey(), entry.getValue()))
        .collect(Collectors.toList());
  }
}
