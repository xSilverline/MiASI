package miasi.backend.domains.analisis.services;

import miasi.backend.domains.analisis.simulation.Status;
import miasi.backend.domains.analisis.types.core.DailyState;
import miasi.backend.domains.analisis.types.core.Resource;
import miasi.backend.domains.analisis.types.crew.ConsumptionMode;
import miasi.backend.domains.analisis.types.input.MissionManifest;
import miasi.backend.domains.analisis.types.result.SimulationOutcome;
import miasi.backend.enums.ResourceType;

import java.util.List;

public class SimulationOutcomeEvaluator {

    public SimulationOutcome evaluate(List<DailyState> timeline, MissionManifest manifest) {
        Integer sosCalledSol = null;
        Integer deathSol = null;

        for (DailyState state : timeline) {
            // 1. Szukamy dnia śmierci (pierwszy dzień z ujemnymi zasobami krytycznymi)
            if (deathSol == null && hasAnyResourceDeficit(state.getWarehouse())) {
                deathSol = state.getSol();
            }

            // 2. Szukamy dnia wezwania SOS (pierwszy dzień przejścia na MINIMAL)
            if (sosCalledSol == null && state.getMode() == ConsumptionMode.MINIMAL) {
                sosCalledSol = state.getSol();
            }
        }

        // Kalkulujemy dzień przylotu ewakuacji
        Integer evacuationSol = (sosCalledSol != null) ? sosCalledSol + manifest.getRescueSols() : null;
        Status status;

        if (deathSol == null) {
            status = Status.SUCCESS;
        } else if (evacuationSol != null && evacuationSol <= deathSol) {
            status = Status.EVACUATION;
        } else {
            status = Status.FAILURE;
        }

        return new SimulationOutcome(status, deathSol, evacuationSol);
    }

    private boolean hasAnyResourceDeficit(List<Resource> warehouse) {
        return warehouse.stream().anyMatch(r ->
                (r.getType() == ResourceType.OXYGEN ||
                        r.getType() == ResourceType.WATER ||
                        r.getType() == ResourceType.FOOD) && r.getAmount() < 0
        );
    }
}