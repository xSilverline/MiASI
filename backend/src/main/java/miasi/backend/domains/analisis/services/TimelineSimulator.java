package miasi.backend.domains.analisis.services;

import lombok.RequiredArgsConstructor;
import miasi.backend.domains.analisis.simulation.SimulationVariant;
import miasi.backend.domains.analisis.simulation.Status;
import miasi.backend.domains.analisis.simulation.VariantType;
import miasi.backend.domains.analisis.types.core.DailyBalance;
import miasi.backend.domains.analisis.types.core.DailyState;
import miasi.backend.domains.analisis.types.core.Resource;
import miasi.backend.domains.analisis.types.crew.ConsumptionMode;
import miasi.backend.domains.analisis.types.input.MissionManifest;
import miasi.backend.domains.analisis.types.modules.Module;
import miasi.backend.domains.analisis.types.schedule.Delivery;
import miasi.backend.enums.ModuleState;
import miasi.backend.enums.ResourceType;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class TimelineSimulator {

    private final DemandCalculator demandCalculator;
    private final ProductionCalculator productionCalculator;
    private final DeliveryProcessor deliveryProcessor;
    private final ThreatProcessor threatProcessor;

    public SimulationVariant simulate(MissionManifest manifest, List<Module> activeModules, List<Resource> startingResources, VariantType variantType) {
        List<Resource> warehouse = copyResources(startingResources);
        List<Module> currentModules = copyModules(activeModules);

        int totalDays = manifest.getDurationSols() + manifest.getRescueSols();
        SimulationVariant variantResult = new SimulationVariant(variantType, Status.IN_PROGRESS, new ArrayList<>());

        for (int sol = 1; sol <= totalDays; sol++) {
            // Logika pojedynczego dnia
            DailyState dailyState = simulateSingleDay(sol, totalDays, warehouse, currentModules, manifest);
            variantResult.getTimeline().add(dailyState);

            // Sprawdzamy warunek przegranej
            if (checkFailureConditions(warehouse)) {
                variantResult.setStatus(Status.FAILURE);
                break;
            }
        }

        if (variantResult.getStatus() != Status.FAILURE) {
            variantResult.setStatus(Status.SUCCESS);
        }

        return variantResult;
    }

    private DailyState simulateSingleDay(int sol, int totalDays, List<Resource> warehouse, List<Module> currentModules, MissionManifest manifest) {
        // KROK A: Zdarzenia zewnętrzne
        deliveryProcessor.process(sol, manifest.getDeliveries(), currentModules, warehouse);
        threatProcessor.process(sol, manifest.getThreats(), currentModules, warehouse);

        // KROK B: Reakcja bazy (Zarządzanie kryzysowe - Total Blackout)
        checkPowerFailures(warehouse, currentModules);
        ConsumptionMode currentMode = evaluateCrewConsumptionMode(sol, totalDays, warehouse, currentModules, manifest);

        // KROK C: Akumulacja bilansu do Twojego mądrego obiektu DailyBalance
        DailyBalance todayBalance = new DailyBalance();

        productionCalculator.calculateModulesProduction(currentModules)
                .forEach(todayBalance::addProduction);

        demandCalculator.calculateCrewDemand(manifest.getCrew(), currentMode)
                .forEach(todayBalance::addConsumption);

        demandCalculator.calculateModulesDemand(currentModules)
                .forEach(todayBalance::addConsumption);

        // KROK D: Aktualizacja magazynu za pomocą logiki z DailyBalance
        List<Resource> updatedWarehouse = todayBalance.applyTo(warehouse);
        warehouse.clear();
        warehouse.addAll(updatedWarehouse);

        // KROK E: Zwracamy pełny stan dnia
        return new DailyState(sol, copyResources(warehouse), todayBalance, currentMode, copyModules(currentModules));
    }

    // --- LOGIKA ZARZĄDZANIA ENERGIĄ (TOTAL BLACKOUT) ---

    private void checkPowerFailures(List<Resource> warehouse, List<Module> currentModules) {
        float powerInWarehouse = getSpecificResourceAmount(warehouse, ResourceType.ENERGY);

        float powerProduced = getSpecificResourceAmount(productionCalculator.calculateModulesProduction(currentModules), ResourceType.ENERGY);
        float powerConsumed = getSpecificResourceAmount(demandCalculator.calculateModulesDemand(currentModules), ResourceType.ENERGY);

        // Jeśli w magazynie jest debet LUB dzisiejsza produkcja razem z zapasami nie pokryje zużycia
        if (powerInWarehouse < 0 || (powerProduced + powerInWarehouse < powerConsumed)) {
            // Wyłączamy absolutnie wszystko
            for (Module module : currentModules) {
                module.setStatus(ModuleState.INACTIVE);
            }
        }
    }

    // --- LOGIKA RACJONOWANIA (DYNAMICZNA) ---

    private ConsumptionMode evaluateCrewConsumptionMode(int currentSol, int totalDays, List<Resource> warehouse, List<Module> currentModules, MissionManifest manifest) {
        List<Resource> optimalCrewDemand = demandCalculator.calculateCrewDemand(manifest.getCrew(), ConsumptionMode.OPTIMAL);
        List<Resource> modulesDemand = demandCalculator.calculateModulesDemand(currentModules);
        List<Resource> production = productionCalculator.calculateModulesProduction(currentModules);

        for (Resource demand : optimalCrewDemand) {
            ResourceType type = demand.getType();
            if (type != ResourceType.OXYGEN && type != ResourceType.WATER && type != ResourceType.FOOD) continue;

            float currentAmount = getSpecificResourceAmount(warehouse, type);
            float dailyProd = getSpecificResourceAmount(production, type);
            float dailyModDemand = getSpecificResourceAmount(modulesDemand, type);

            float netBalance = dailyProd - (dailyModDemand + demand.getAmount());

            if (netBalance >= 0) continue;

            float dailyLoss = Math.abs(netBalance);
            float daysLeftUntilEmpty = currentAmount / dailyLoss;

            int nextDeliverySol = findNextDeliverySolForResource(currentSol, type, manifest.getDeliveries());
            int targetSol = (nextDeliverySol != -1) ? nextDeliverySol : totalDays;
            int daysUntilTarget = targetSol - currentSol + 1;

            if (daysLeftUntilEmpty <= daysUntilTarget) {
                return ConsumptionMode.MINIMAL;
            }
        }
        return ConsumptionMode.OPTIMAL;
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

    // --- METODY POMOCNICZE ---

    private boolean checkFailureConditions(List<Resource> warehouse) {
        for (Resource resource : warehouse) {
            if ((resource.getType() == ResourceType.OXYGEN ||
                    resource.getType() == ResourceType.WATER ||
                    resource.getType() == ResourceType.FOOD) &&
                    resource.getAmount() < 0) {
                return true;
            }
        }
        return false;
    }

    private float getSpecificResourceAmount(List<Resource> resources, ResourceType type) {
        return resources.stream()
                .filter(r -> r.getType() == type)
                .map(Resource::getAmount)
                .findFirst()
                .orElse(0f);
    }

    private List<Resource> copyResources(List<Resource> source) {
        if (source == null) return new ArrayList<>();
        return source.stream()
                .map(Resource::copy)
                .collect(java.util.stream.Collectors.toCollection(ArrayList::new));
    }

    private List<Module> copyModules(List<Module> source) {
        if (source == null) return new ArrayList<>();
        return source.stream()
                .map(Module::copy)
                .collect(java.util.stream.Collectors.toCollection(ArrayList::new));
    }
}