package miasi.backend.domains.analysis.domain._simulation; // Zmieniony pakiet na poprawny w DDD

import java.util.List;
import miasi.backend.domains.analysis.domain.core.DailyState;
import miasi.backend.domains.analysis.domain.core.MissionManifest;
import miasi.backend.domains.analysis.domain.core.ObservationType;
import miasi.backend.domains.analysis.domain.core.Resource;
import miasi.backend.domains.analysis.domain.core.ResourceType;
import miasi.backend.domains.analysis.domain.core.Status;


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
      if (sosCalledSol == null && state.getObservations()
          .contains(ObservationType.EVACUATION_ALERT)) {
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

  /**
   * Metoda przeniesiona z BaselineModuleRecommender. Oblicza, ile zasobów potrzeba zabrać na start,
   * znajdując największe dołki na osi czasu.
   */
  public List<Resource> calculateMinimumSurvivalSupplies(List<DailyState> timeline) {
    float minOxygen = 0, minWater = 0, minFood = 0;

    for (DailyState state : timeline) {
      minOxygen = Math.min(minOxygen, getResourceAmount(state.getWarehouse(), ResourceType.OXYGEN));
      minWater = Math.min(minWater, getResourceAmount(state.getWarehouse(), ResourceType.WATER));
      minFood = Math.min(minFood, getResourceAmount(state.getWarehouse(), ResourceType.FOOD));
    }

    // Zwracamy wartości absolutne – skoro spadliśmy np. do -50, to na start potrzebujemy +50.
    return List.of(
        new Resource(ResourceType.OXYGEN, Math.abs(minOxygen)),
        new Resource(ResourceType.WATER, Math.abs(minWater)),
        new Resource(ResourceType.FOOD, Math.abs(minFood))
    );
  }

  // --- METODY POMOCNICZE ---

  private boolean hasAnyResourceDeficit(List<Resource> warehouse) {
    return warehouse.stream().anyMatch(r ->
        (r.getType() == ResourceType.OXYGEN ||
            r.getType() == ResourceType.WATER ||
            r.getType() == ResourceType.FOOD) && r.getAmount() < 0
    );
  }

  private float getResourceAmount(List<Resource> warehouse, ResourceType type) {
    return warehouse.stream()
        .filter(r -> r.getType() == type)
        .map(Resource::getAmount)
        .findFirst()
        .orElse(0f);
  }
}