package miasi.backend.domains.analisis.services;

import miasi.backend.domains.analisis.types.core.Resource;
import miasi.backend.domains.analisis.types.crew.ConsumptionMode;
import miasi.backend.domains.analisis.types.crew.CrewGroup;
import miasi.backend.domains.analisis.types.modules.Module;
import miasi.backend.enums.ModuleState;
import miasi.backend.enums.ResourceType;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DemandCalculator {

  // sum up the requirements of each crew group depending on the selected rationing mode
  public List<Resource> calculateCrewDemand(List<CrewGroup> crewGroups, ConsumptionMode mode) {
    Map<ResourceType, Float> dailyCrewDemand = new EnumMap<>(ResourceType.class);

    for (CrewGroup group : crewGroups) {

      // Get the required resources per person based on the mode (OPTIMAL or MINIMAL)
      List<Resource> perPersonDemand = group.getDailyDemand(mode);

      int peopleCount = group.getCount();

      for (Resource res : perPersonDemand) {
        float totalAmount = res.getAmount() * peopleCount;

        // Merge into the total daily crew demand map
        dailyCrewDemand.merge(res.getType(), totalAmount, Float::sum);
      }
    }

    return dailyCrewDemand.entrySet().stream()
        .map(entry -> new Resource(entry.getKey(), entry.getValue()))
        .collect(Collectors.toList());
  }

  // Sum up the resources consumed only by currently ACTIVE modules
  public List<Resource> calculateModulesDemand(List<Module> currentModules) {
    Map<ResourceType, Float> dailyModulesDemand = new EnumMap<>(ResourceType.class);

    for (Module module : currentModules) {

      if (module.getStatus() == ModuleState.ACTIVE) {

        for (Resource res : module.getConsumption()) {

          // Multiply by efficiency
          float actualAmount = res.getAmount() * module.getEfficiency();

          dailyModulesDemand.merge(res.getType(), actualAmount, Float::sum);
        }
      }
    }

    // Convert the map with sums back into a List of Resources
    return dailyModulesDemand.entrySet().stream()
        .map(entry -> new Resource(entry.getKey(), entry.getValue()))
        .collect(Collectors.toList());
  }
}