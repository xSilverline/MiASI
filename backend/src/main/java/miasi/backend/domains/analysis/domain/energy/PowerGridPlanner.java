package miasi.backend.domains.analysis.domain.energy;

import java.util.List;
import lombok.RequiredArgsConstructor;
import miasi.backend.domains.analysis.domain.core.Resource;
import miasi.backend.domains.analysis.domain.core.ResourceType;
import miasi.backend.domains.analysis.domain.crew.DemandCalculator;
import miasi.backend.domains.analysis.domain.modules.Module;
import miasi.backend.domains.analysis.domain.modules.ProductionCalculator;

@RequiredArgsConstructor
public class PowerGridPlanner {

  private final ProductionCalculator productionCalculator;
  private final DemandCalculator demandCalculator;

  public void resolvePowerDeficit(List<Module> currentModules, List<Module> catalog) {
    int safetyCounter = 0;

    while (calculatePowerBalance(currentModules) < 0 && safetyCounter < 50) {
      Module bestGenerator = findMostEfficientGenerator(catalog);

      if (bestGenerator != null) {
        currentModules.add(bestGenerator.copy());
      } else {
        break;
      }
      safetyCounter++;
    }
  }

  private float calculatePowerBalance(List<Module> modules) {
    float powerProduced = getEnergyAmount(productionCalculator.calculateModulesProduction(modules));
    float powerConsumed = getEnergyAmount(demandCalculator.calculateModulesDemand(modules));
    return powerProduced - powerConsumed;
  }

  private Module findMostEfficientGenerator(List<Module> catalog) {
    return catalog.stream()
        .filter(this::isNetPowerGenerator)
        .max((m1, m2) -> Float.compare(getNetPower(m1), getNetPower(m2)))
        .orElse(null);
  }

  private boolean isNetPowerGenerator(Module module) {
    return getNetPower(module) > 0;
  }

  private float getNetPower(Module module) {
    float produced =
        getEnergyAmount(productionCalculator.calculateModulesProduction(List.of(module)));
    float consumed = getEnergyAmount(demandCalculator.calculateModulesDemand(List.of(module)));
    return produced - consumed;
  }

  private float getEnergyAmount(List<Resource> resources) {
    if (resources == null) {
      return 0f;
    }
    return resources.stream()
        .filter(r -> r.getType() == ResourceType.ENERGY)
        .map(Resource::getAmount)
        .findFirst()
        .orElse(0f);
  }
}
