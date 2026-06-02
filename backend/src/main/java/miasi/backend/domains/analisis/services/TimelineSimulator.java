package miasi.backend.domains.analisis.services;

import lombok.RequiredArgsConstructor;
import miasi.backend.domains.analisis.simulation.SimulationVariant;
import miasi.backend.domains.analisis.simulation.VariantType;
import miasi.backend.domains.analisis.types.core.Resource;
import miasi.backend.domains.analisis.types.crew.ConsumptionMode;
import miasi.backend.domains.analisis.types.input.MissionManifest;
import miasi.backend.domains.analisis.types.modules.Module;
import miasi.backend.domains.analisis.types.schedule.Threat;

import java.util.List;

@RequiredArgsConstructor
public class TimelineSimulator {

    private final DemandCalculator demandCalculator;
    private final ProductionCalculator productionCalculator;
    private final DeliveryProcessor deliveryProcessor;
    private final ThreatProcessor threatProcessor;

    public SimulationVariant simulate(MissionManifest manifest, List<Module> activeModules, List<Resource> startingResources, List<Threat> threats, VariantType variantType) {
        // pętla od 1 do (durationSols + rescueSols):
            // przetwarzaj dostawy/awarie,
            // licz popyt/produkcję,
            // buduj DailyState
            // sprawdzaj warunki porażki
        return null;
    }

    private void checkPowerFailures(List<Resource> warehouse, List<Module> activeModules) {
        // wylicz bilans prądu i wyłącz moduły (zmiana na INACTIVE), jeśli zużycie przekracza produkcję
    }

    private ConsumptionMode evaluateCrewConsumptionMode(List<Resource> warehouse, MissionManifest manifest) {
        // zbadaj stan krytycznych zapasów w magazynie i zadecyduj, czy załoga musi przejść w tryb MINIMAL
        return null;
    }
}