package miasi.backend.domains.analysis.domain._payload;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import miasi.backend.domains.analysis.domain._simulation.SimulationOutcomeEvaluator;
import miasi.backend.domains.analysis.domain._simulation.TimelineSimulator;
import miasi.backend.domains.analysis.domain.core.DailyState;
import miasi.backend.domains.analysis.domain.core.MissionManifest;
import miasi.backend.domains.analysis.domain.core.Resource;
import miasi.backend.domains.analysis.domain.core.ResourceType;
import miasi.backend.domains.analysis.domain.energy.PowerGridPlanner;
import miasi.backend.domains.analysis.domain.modules.Module;

@RequiredArgsConstructor
public class PayloadWeightOptimizer {

  private final WeightCalculator weightCalculator;
  private final TimelineSimulator timelineSimulator;
  private final PowerGridPlanner powerGridPlanner;
  private final SimulationOutcomeEvaluator evaluator;

  public OptimalConfiguration optimizeConfiguration(
      MissionManifest manifest, List<Module> catalog) {

    List<Module> activeModules = initializeMandatoryModules(catalog);

    powerGridPlanner.resolvePowerDeficit(activeModules, catalog);

    List<Resource> bestSol0Supplies = simulateAndFindDeficit(activeModules, manifest);
    float bestTotalWeight = weightCalculator.calculateTotalWeight(activeModules, bestSol0Supplies);

    boolean optimizationPossible = true;
    while (optimizationPossible) {
      optimizationPossible = false;

      Module bestCandidate = null;
      List<Module> bestCandidateModules = null;
      List<Resource> bestCandidateSupplies = null;
      float lowestWeightInIteration = bestTotalWeight;

      for (Module candidate : catalog) {
        if (countModules(activeModules, candidate) >= candidate.getMaxCount()) {
          continue;
        }

        List<Module> testModules = copyModules(activeModules);
        testModules.add(candidate.copy());

        powerGridPlanner.resolvePowerDeficit(testModules, catalog);

        List<Resource> testSupplies = simulateAndFindDeficit(testModules, manifest);
        float testWeight = weightCalculator.calculateTotalWeight(testModules, testSupplies);

        if (testWeight < lowestWeightInIteration) {
          lowestWeightInIteration = testWeight;
          bestCandidate = candidate;
          bestCandidateModules = testModules;
          bestCandidateSupplies = testSupplies;
        }
      }

      if (bestCandidate != null) {
        activeModules = bestCandidateModules;
        bestSol0Supplies = bestCandidateSupplies;
        bestTotalWeight = lowestWeightInIteration;
        optimizationPossible = true;
      }
    }

    boolean isLimitExceeded = isWeightLimitExceeded(bestTotalWeight, manifest);
    return new OptimalConfiguration(
        activeModules, bestSol0Supplies, bestTotalWeight, isLimitExceeded);
  }

  private List<Resource> simulateAndFindDeficit(List<Module> modules, MissionManifest manifest) {
    List<Resource> emptyWarehouse =
        List.of(
            new Resource(ResourceType.OXYGEN, 0),
            new Resource(ResourceType.WATER, 0),
            new Resource(ResourceType.FOOD, 0));

    List<DailyState> timeline = timelineSimulator.simulate(manifest, modules, emptyWarehouse);
    return evaluator.calculateMinimumSurvivalSupplies(timeline);
  }

  private List<Module> initializeMandatoryModules(List<Module> catalog) {
    List<Module> mandatory = new ArrayList<>();
    for (Module module : catalog) {
      for (int i = 0; i < module.getMinCount(); i++) {
        mandatory.add(module.copy());
      }
    }
    return mandatory;
  }

  private long countModules(List<Module> currentModules, Module target) {
    return currentModules.stream().filter(m -> m.getId().equals(target.getId())).count();
  }

  private boolean isWeightLimitExceeded(float totalWeight, MissionManifest manifest) {
    return false;
  }

  private List<Module> copyModules(List<Module> source) {
    return source.stream()
        .map(Module::copy)
        .collect(java.util.stream.Collectors.toCollection(ArrayList::new));
  }
}
