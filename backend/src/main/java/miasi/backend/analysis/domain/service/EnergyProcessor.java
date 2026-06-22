package miasi.backend.analysis.domain.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import miasi.backend.analysis.domain.model.core.Resource;
import miasi.backend.analysis.domain.model.modules.Module;
import miasi.backend.common.domain.model.ModuleState;
import miasi.backend.common.domain.model.ResourceType;

@RequiredArgsConstructor
public class EnergyProcessor {

  private final ProductionCalculator productionCalculator;
  private final DemandCalculator demandCalculator;

  public boolean process(float availableEnergy, List<Module> currentModules) {

    float powerProduced =
        getEnergyAmount(productionCalculator.calculateModulesProduction(currentModules));
    float powerConsumed = getEnergyAmount(demandCalculator.calculateModulesDemand(currentModules));

    if (availableEnergy < 0 || (powerProduced + availableEnergy < powerConsumed)) {
      for (Module module : currentModules) {
        module.setStatus(ModuleState.INACTIVE);
      }
      return true;
    }
    return false;
  }

  public float getEnergyAmount(List<Resource> resources) {
    if (resources == null) return 0f;
    return resources.stream()
        .filter(r -> r.getType() == ResourceType.ENERGY)
        .map(Resource::getAmount)
        .findFirst()
        .orElse(0f);
  }
}
