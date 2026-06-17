package miasi.backend.domains.analisis.services;

import miasi.backend.domains.analisis.types.core.Resource;
import miasi.backend.domains.analisis.types.modules.Module;
import miasi.backend.enums.ModuleState;
import miasi.backend.enums.ResourceType;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ProductionCalculator {

  public List<Resource> calculateModulesProduction(List<Module> currentModules) {
    // Sum up the resources produced only by currently ACTIVE modules
    Map<ResourceType, Float> dailyProduction = new EnumMap<>(ResourceType.class);
    for (Module module : currentModules) {

      // Process only active modules
      if (module.getStatus() == ModuleState.ACTIVE) {

        // Iterate through the list of resources produced by this module
        for (Resource res : module.getProduction()) {
          float actualAmount = res.getAmount() * module.getEfficiency();

          // Add to the daily production map and sum with the existing value
          dailyProduction.merge(res.getType(), actualAmount, Float::sum);
        }
      }
    }
    // Convert the map with sums back into a List of Resources
    return dailyProduction.entrySet().stream()
        .map(entry -> new Resource(entry.getKey(), entry.getValue()))
        .collect(Collectors.toList());
  }
}