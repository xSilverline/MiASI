package miasi.backend.domains.analisis.services;

import lombok.RequiredArgsConstructor;
import miasi.backend.domains.analisis.types.core.Resource;
import miasi.backend.domains.analisis.types.crew.ConsumptionMode;
import miasi.backend.domains.analisis.types.input.MissionManifest;
import miasi.backend.domains.analisis.types.modules.Module;
import miasi.backend.domains.analisis.types.schedule.Delivery;
import miasi.backend.enums.ResourceType;

import java.util.List;

@RequiredArgsConstructor
public class SurvivalPredictor {

    private final DemandCalculator demandCalculator;
    private final ProductionCalculator productionCalculator;

    // Kiedy przechodzimy na MINIMAL? Kiedy OPTIMAL nas zabije przed dostawą.
    public ConsumptionMode evaluateCrewConsumptionMode(int currentSol, int targetSol, List<Resource> warehouse, List<Module> currentModules, MissionManifest manifest) {
        if (willDieBeforeTarget(currentSol, targetSol, warehouse, currentModules, manifest, ConsumptionMode.OPTIMAL)) {
            return ConsumptionMode.MINIMAL;
        }
        return ConsumptionMode.OPTIMAL;
    }

    // Kiedy wzywamy SOS? Kiedy nawet MINIMAL nas zabije przed dostawą!
    public boolean checkIfEvacuationIsNeeded(int currentSol, int targetSol, List<Resource> warehouse, List<Module> currentModules, MissionManifest manifest) {
        return willDieBeforeTarget(currentSol, targetSol, warehouse, currentModules, manifest, ConsumptionMode.MINIMAL);
    }

    // --- Silnik liczący przewidywaną śmierć ---
    private boolean willDieBeforeTarget(int currentSol, int missionEndSol, List<Resource> warehouse, List<Module> currentModules, MissionManifest manifest, ConsumptionMode modeToCheck) {
        List<Resource> demand = demandCalculator.calculateCrewDemand(manifest.getCrew(), modeToCheck);
        List<Resource> modulesDemand = demandCalculator.calculateModulesDemand(currentModules);
        List<Resource> production = productionCalculator.calculateModulesProduction(currentModules);

        for (Resource res : demand) {
            ResourceType type = res.getType();
            if (type != ResourceType.OXYGEN && type != ResourceType.WATER && type != ResourceType.FOOD) continue;

            float currentAmount = getSpecificResourceAmount(warehouse, type);
            float dailyProd = getSpecificResourceAmount(production, type);
            float dailyModDemand = getSpecificResourceAmount(modulesDemand, type);

            float netBalance = dailyProd - (dailyModDemand + res.getAmount());

            if (netBalance >= 0) continue; // Mamy nadwyżkę lub zero, jesteśmy bezpieczni

            float daysLeftUntilEmpty = currentAmount / Math.abs(netBalance);

            int nextDeliverySol = findNextDeliverySolForResource(currentSol, type, manifest.getDeliveries());

            int daysUntilTarget;
            if (nextDeliverySol != -1) {
                // Cel to DOSTAWA: Wystarczy dotrwać do poranka w dniu dostawy (bez dodawania +1)
                daysUntilTarget = nextDeliverySol - currentSol;
            } else {
                // Cel to KONIEC MISJI: Trzeba przeżyć pełne dni, łącznie z ostatnim dniem misji (wymaga +1)
                daysUntilTarget = missionEndSol - currentSol + 1;
            }

            // Zginiemy szybciej, niż nadejdzie cel (wynik 0.0 w magazynie to wciąż życie)
            if (daysLeftUntilEmpty < daysUntilTarget) {
                return true;
            }
        }
        return false;
    }

    private int findNextDeliverySolForResource(int currentSol, ResourceType type, List<Delivery> deliveries) {
        if (deliveries == null) return -1;
        return deliveries.stream()
                .filter(d -> d.getSol() > currentSol)
                .filter(d -> containsResource(d.getResources(), type))
                .map(Delivery::getSol)
                .min(Integer::compareTo)
                .orElse(-1);
    }

    private boolean containsResource(List<Resource> resources, ResourceType type) {
        if (resources == null) return false;
        return resources.stream().anyMatch(r -> r.getType() == type && r.getAmount() > 0);
    }

    private float getSpecificResourceAmount(List<Resource> resources, ResourceType type) {
        if (resources == null) return 0f;
        return resources.stream()
                .filter(r -> r.getType() == type)
                .map(Resource::getAmount)
                .findFirst()
                .orElse(0f);
    }
}