package miasi.backend.domains.analysis.domain.crew;

import java.util.List;
import lombok.RequiredArgsConstructor;
import miasi.backend.domains.analysis.domain.core.MissionManifest;
import miasi.backend.domains.analysis.domain.core.Resource;
import miasi.backend.domains.analysis.domain.core.ResourceType;
import miasi.backend.domains.analysis.domain.modules.Module;
import miasi.backend.domains.analysis.domain.modules.ProductionCalculator;
import miasi.backend.domains.analysis.domain.schedule.Delivery;

@RequiredArgsConstructor
public class SurvivalPredictor {

  private final DemandCalculator demandCalculator;
  private final ProductionCalculator productionCalculator;

  public ConsumptionMode evaluateCrewConsumptionMode(int currentSol, int targetSol,
      List<Resource> warehouse, List<Module> currentModules, MissionManifest manifest) {
    if (willDieBeforeTarget(currentSol, targetSol, warehouse, currentModules, manifest,
        ConsumptionMode.OPTIMAL)) {
      return ConsumptionMode.MINIMAL;
    }
    return ConsumptionMode.OPTIMAL;
  }

  public boolean checkIfEvacuationIsNeeded(int currentSol, int targetSol, List<Resource> warehouse,
      List<Module> currentModules, MissionManifest manifest) {
    return willDieBeforeTarget(currentSol, targetSol, warehouse, currentModules, manifest,
        ConsumptionMode.MINIMAL);
  }

  private boolean willDieBeforeTarget(int currentSol, int missionEndSol, List<Resource> warehouse,
      List<Module> currentModules, MissionManifest manifest, ConsumptionMode modeToCheck) {
    List<Resource> demand = demandCalculator.calculateCrewDemand(manifest.getCrew(), modeToCheck);
    List<Resource> modulesDemand = demandCalculator.calculateModulesDemand(currentModules);
    List<Resource> production = productionCalculator.calculateModulesProduction(currentModules);

    for (Resource res : demand) {
      ResourceType type = res.getType();
      if (type != ResourceType.OXYGEN && type != ResourceType.WATER && type != ResourceType.FOOD) {
        continue;
      }

      float currentAmount = getSpecificResourceAmount(warehouse, type);
      float dailyProd = getSpecificResourceAmount(production, type);
      float dailyModDemand = getSpecificResourceAmount(modulesDemand, type);

      float netBalance = dailyProd - (dailyModDemand + res.getAmount());

      if (netBalance >= 0) {
        continue;
      }

      float daysLeftUntilEmpty = currentAmount / Math.abs(netBalance);

      int nextDeliverySol = findNextDeliverySolForResource(currentSol, type,
          manifest.getDeliveries());

      int daysUntilTarget;
      if (nextDeliverySol != -1) {
        daysUntilTarget = nextDeliverySol - currentSol;
      } else {
        daysUntilTarget = missionEndSol - currentSol + 1;
      }

      if (daysLeftUntilEmpty < daysUntilTarget) {
        return true;
      }
    }
    return false;
  }

  private int findNextDeliverySolForResource(int currentSol, ResourceType type,
      List<Delivery> deliveries) {
    if (deliveries == null) {
      return -1;
    }
    return deliveries.stream()
        .filter(d -> d.getSol() > currentSol)
        .filter(d -> containsResource(d.getResources(), type))
        .map(Delivery::getSol)
        .min(Integer::compareTo)
        .orElse(-1);
  }

  private boolean containsResource(List<Resource> resources, ResourceType type) {
    if (resources == null) {
      return false;
    }
    return resources.stream().anyMatch(r -> r.getType() == type && r.getAmount() > 0);
  }

  private float getSpecificResourceAmount(List<Resource> resources, ResourceType type) {
    if (resources == null) {
      return 0f;
    }
    return resources.stream()
        .filter(r -> r.getType() == type)
        .map(Resource::getAmount)
        .findFirst()
        .orElse(0f);
  }
}