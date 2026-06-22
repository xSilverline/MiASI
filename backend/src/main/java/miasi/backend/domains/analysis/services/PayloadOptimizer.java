package miasi.backend.domains.analysis.services;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import miasi.backend.domains.analysis.types.core.DailyState;
import miasi.backend.domains.analysis.types.core.Resource;
import miasi.backend.domains.analysis.types.input.MissionManifest;
import miasi.backend.domains.analysis.types.modules.Module;
import miasi.backend.domains.analysis.types.result.OptimalConfiguration;
import miasi.backend.sharedkernel.model.ResourceType;

@RequiredArgsConstructor
public class PayloadOptimizer {

  private final WeightCalculator weightCalculator;
  private final TimelineSimulator timelineSimulator;

  public OptimalConfiguration findOptimalConfiguration(MissionManifest manifest) {
    if (manifest == null) {
      throw new IllegalArgumentException("Mission manifest is required");
    }

    List<Module> activeModules = initializeMandatoryModules(safeCatalog(manifest));
    regulatePower(activeModules, manifest);

    List<Resource> startingResources = calculateSolZeroSupplies(activeModules, manifest);
    float totalWeight = weightCalculator.calculateTotalWeight(activeModules, startingResources);

    return new OptimalConfiguration(
        List.copyOf(activeModules),
        List.copyOf(startingResources),
        totalWeight,
        weightCalculator.isLimitExceeded(totalWeight, manifest.getMaxWeightSolZero()));
  }

  private List<Module> initializeMandatoryModules(List<Module> catalog) {
    List<Module> activeModules = new ArrayList<>();
    for (Module module : catalog) {
      for (int count = 0; count < module.getMinCount(); count++) {
        activeModules.add(module.copy());
      }
    }
    return activeModules;
  }

  private void regulatePower(List<Module> currentModules, MissionManifest manifest) {
    List<Module> catalog = safeCatalog(manifest);
    while (energyBalance(currentModules) < 0) {
      Module candidate =
          catalog.stream()
              .filter(module -> energyBalance(List.of(module)) > 0)
              .filter(module -> canAddMore(currentModules, module))
              .min(Comparator.comparing(Module::getWeight))
              .orElse(null);

      if (candidate == null) {
        return;
      }

      currentModules.add(candidate.copy());
    }
  }

  private List<Resource> calculateSolZeroSupplies(
      List<Module> testModules, MissionManifest manifest) {
    List<DailyState> timeline =
        timelineSimulator.simulate(manifest, new ArrayList<>(testModules), Collections.emptyList());
    Map<ResourceType, Float> lowestAmounts = new EnumMap<>(ResourceType.class);

    for (DailyState state : timeline) {
      for (Resource resource : state.getWarehouse()) {
        lowestAmounts.merge(resource.getType(), resource.getAmount(), Math::min);
      }
    }

    return lowestAmounts.entrySet().stream()
        .filter(entry -> entry.getValue() < 0)
        .map(entry -> new Resource(entry.getKey(), Math.abs(entry.getValue())))
        .toList();
  }

  private List<Module> safeCatalog(MissionManifest manifest) {
    return manifest.getCatalog() == null ? List.of() : manifest.getCatalog();
  }

  private boolean canAddMore(List<Module> currentModules, Module candidate) {
    if (candidate.getMaxCount() == null) {
      return true;
    }

    long currentCount =
        currentModules.stream()
            .filter(module -> Objects.equals(module.getName(), candidate.getName()))
            .count();
    return currentCount < candidate.getMaxCount();
  }

  private float energyBalance(List<Module> modules) {
    float produced = 0f;
    float consumed = 0f;

    for (Module module : modules) {
      produced += energyAmount(module.getProduction());
      consumed += energyAmount(module.getConsumption());
    }

    return produced - consumed;
  }

  private float energyAmount(List<Resource> resources) {
    if (resources == null) {
      return 0f;
    }

    return resources.stream()
        .filter(resource -> resource.getType() == ResourceType.ENERGY)
        .map(Resource::getAmount)
        .reduce(0f, Float::sum);
  }
}
