package miasi.backend.domains.analysis.domain.energy;

import java.util.List;
import lombok.RequiredArgsConstructor;
import miasi.backend.domains.analysis.domain.core.Resource;
import miasi.backend.domains.analysis.domain.core.ResourceType;
import miasi.backend.domains.analysis.domain.crew.DemandCalculator;
import miasi.backend.domains.analysis.domain.modules.Module;
import miasi.backend.domains.analysis.domain.modules.ModuleState;
import miasi.backend.domains.analysis.domain.modules.ProductionCalculator;

@RequiredArgsConstructor
public class PowerGridSimulator {

  private final ProductionCalculator productionCalculator;
  private final DemandCalculator demandCalculator;

  public boolean process(float availableEnergy, List<Module> currentModules) {

    float powerProduced = getEnergyAmount(
        productionCalculator.calculateModulesProduction(currentModules));
    float powerConsumed = getEnergyAmount(demandCalculator.calculateModulesDemand(currentModules));

    if (availableEnergy < 0 || (powerProduced + availableEnergy < powerConsumed)) {
      currentModules.replaceAll(module -> module.withStatus(ModuleState.INACTIVE));
      return true;
    }
    return false;
  }

  public float getEnergyAmount(List<Resource> resources) {
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