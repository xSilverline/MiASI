package miasi.backend.analysis.domain.service;

import java.util.List;
import miasi.backend.analysis.domain.model.core.DailyState;
import miasi.backend.analysis.domain.model.core.ObservationType;
import miasi.backend.analysis.domain.model.core.Resource;
import miasi.backend.analysis.domain.model.input.MissionManifest;
import miasi.backend.analysis.domain.model.result.SimulationOutcome;
import miasi.backend.analysis.domain.model.simulation.Status;
import miasi.backend.common.domain.model.ResourceType;

public class SimulationOutcomeEvaluator {

  public SimulationOutcome evaluate(List<DailyState> timeline, MissionManifest manifest) {
    Integer sosCalledSol = null;
    Integer deathSol = null;

    for (DailyState state : timeline) {
      // 1. Szukamy dnia śmierci (pierwszy dzień z ujemnymi zasobami krytycznymi)
      if (deathSol == null && hasAnyResourceDeficit(state.getWarehouse())) {
        deathSol = state.getSol();
      }

      // 2. Szukamy dnia wezwania SOS
      if (sosCalledSol == null
          && state.getObservations().contains(ObservationType.EVACUATION_ALERT)) {
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
    return warehouse.stream()
        .anyMatch(
            r ->
                (r.getType() == ResourceType.OXYGEN
                        || r.getType() == ResourceType.WATER
                        || r.getType() == ResourceType.FOOD)
                    && r.getAmount() < 0);
  }
}
