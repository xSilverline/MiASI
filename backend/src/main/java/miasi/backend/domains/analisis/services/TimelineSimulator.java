package miasi.backend.domains.analisis.services;

import lombok.RequiredArgsConstructor;
import miasi.backend.domains.analisis.simulation.SimulationVariant;
import miasi.backend.domains.analisis.simulation.Status;
import miasi.backend.domains.analisis.simulation.VariantType;
import miasi.backend.domains.analisis.types.core.DailyBalance;
import miasi.backend.domains.analisis.types.core.DailyState;
import miasi.backend.domains.analisis.types.core.ObservationType;
import miasi.backend.domains.analisis.types.core.Resource;
import miasi.backend.domains.analisis.types.crew.ConsumptionMode;
import miasi.backend.domains.analisis.types.input.MissionManifest;
import miasi.backend.domains.analisis.types.modules.Module;
import miasi.backend.domains.analisis.types.schedule.Delivery;
import miasi.backend.enums.ModuleState;
import miasi.backend.enums.ResourceType;

import javax.annotation.processing.AbstractProcessor;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
public class TimelineSimulator {

    private final DemandCalculator demandCalculator;
    private final ProductionCalculator productionCalculator;
    private final DeliveryProcessor deliveryProcessor;
    private final ThreatProcessor threatProcessor;
    private final EnergyProcessor energyProcessor;
    private final SurvivalPredictor survivalPredictor;



    public List<DailyState> simulate(MissionManifest manifest, List<Module> activeModules, List<Resource> startingResources) {
        List<Resource> warehouse = copyResources(startingResources);
        List<Module> currentModules = copyModules(activeModules);
        List<DailyState> timeline = new ArrayList<>();

        int totalDays = manifest.getDurationSols() + manifest.getRescueSols();

        ConsumptionMode previousMode = ConsumptionMode.OPTIMAL;
        for (int sol = 1; sol <= totalDays; sol++) {
            DailyState dailyState = simulateSingleDay(sol, totalDays, warehouse, currentModules, manifest, previousMode);
            timeline.add(dailyState);
            previousMode = dailyState.getMode();
        }

        return timeline; // Zwracamy tylko czystą historię!
    }

    private DailyState simulateSingleDay(int sol, int totalDays, List<Resource> warehouse, List<Module> currentModules, MissionManifest manifest, ConsumptionMode previousMode) {
        Set<ObservationType> observations = new java.util.HashSet<>();
        processExternalEvents(sol, warehouse, currentModules, manifest, observations);
        processPowerGrid(warehouse, currentModules, observations);
        ConsumptionMode currentMode = evaluateSurvivalAndMode(sol, totalDays, warehouse, currentModules, manifest, previousMode, observations);
        DailyBalance todayBalance = calculateDailyBalance(currentModules, manifest, currentMode);
        updateWarehouse(warehouse, todayBalance);

        return new DailyState(sol, copyResources(warehouse), todayBalance, currentMode, copyModules(currentModules), observations);
    }

    private void processExternalEvents(int sol, List<Resource> warehouse, List<Module> currentModules, MissionManifest manifest, Set<ObservationType> observations) {
        deliveryProcessor.process(sol, manifest.getDeliveries(), currentModules, warehouse);
        threatProcessor.process(sol, manifest.getThreats(), currentModules, warehouse);

        boolean hasDelivery = manifest.getDeliveries() != null &&
                manifest.getDeliveries().stream().anyMatch(d -> d.getSol() == sol);

        if (hasDelivery) {
            observations.add(ObservationType.DELIVERY_RECEIVED);
        }
    }

    private void processPowerGrid(List<Resource> warehouse, List<Module> currentModules, Set<ObservationType> observations) {
        float availableEnergy = getSpecificResourceAmount(warehouse, ResourceType.ENERGY);

        if (energyProcessor.process(availableEnergy, currentModules)) {
            observations.add(ObservationType.TOTAL_BLACKOUT);
        }
    }

    private ConsumptionMode evaluateSurvivalAndMode(int sol, int totalDays, List<Resource> warehouse, List<Module> currentModules, MissionManifest manifest, ConsumptionMode previousMode, Set<ObservationType> observations) {
        ConsumptionMode currentMode = survivalPredictor.evaluateCrewConsumptionMode(sol, totalDays, warehouse, currentModules, manifest);

        if (survivalPredictor.checkIfEvacuationIsNeeded(sol, totalDays, warehouse, currentModules, manifest)) {
            observations.add(ObservationType.EVACUATION_ALERT);
        }

        if (currentMode == ConsumptionMode.MINIMAL && previousMode == ConsumptionMode.OPTIMAL) {
            observations.add(ObservationType.MINIMAL_DEMAND_ACTIVATED);
        } else if (currentMode == ConsumptionMode.OPTIMAL && previousMode == ConsumptionMode.MINIMAL) {
            observations.add(ObservationType.OPTIMAL_DEMAND_ACTIVATED);
        }

        return currentMode;
    }

    private DailyBalance calculateDailyBalance(List<Module> currentModules, MissionManifest manifest, ConsumptionMode currentMode) {
        DailyBalance balance = new DailyBalance();

        productionCalculator.calculateModulesProduction(currentModules).forEach(balance::addProduction);
        demandCalculator.calculateCrewDemand(manifest.getCrew(), currentMode).forEach(balance::addConsumption);
        demandCalculator.calculateModulesDemand(currentModules).forEach(balance::addConsumption);

        return balance;
    }

    private void updateWarehouse(List<Resource> warehouse, DailyBalance balance) {
        List<Resource> updated = balance.applyTo(warehouse);
        warehouse.clear();
        warehouse.addAll(updated);
    }

    // --- METODY POMOCNICZE ---

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