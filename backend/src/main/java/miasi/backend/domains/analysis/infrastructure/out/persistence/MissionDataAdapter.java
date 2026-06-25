package miasi.backend.domains.analysis.infrastructure.out.persistence;

import java.util.List;
import lombok.RequiredArgsConstructor;
import miasi.backend.api.config.ConfService;
import miasi.backend.domains.analysis.application.port.out.IMissionDataProviderPort;
import miasi.backend.domains.analysis.domain.core.MissionManifest;
import miasi.backend.domains.analysis.domain.core.Resource;
import miasi.backend.domains.analysis.domain.core.ResourceType;
import miasi.backend.domains.analysis.domain.crew.ConsumptionProfile;
import miasi.backend.domains.analysis.domain.crew.CrewGroup;
import miasi.backend.domains.analysis.domain.modules.Module;
import miasi.backend.domains.analysis.domain.modules.ModuleState;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MissionDataAdapter implements IMissionDataProviderPort {

  private final ConfService confService;

  @Override
  public MissionManifest getMissionManifest(int missionPlanId) {
    miasi.backend.domains.configuration.missionPlan.MissionPlan oldPlan =
        confService.getMissionPlan(missionPlanId);

    if (oldPlan == null) {
      throw new RuntimeException("Nie znaleziono planu misji o ID: " + missionPlanId);
    }

    List<CrewGroup> mappedCrew =
        oldPlan.getCrew().stream()
            .map(
                oldProfile ->
                    new CrewGroup(
                        oldProfile.getName(),
                        oldProfile.getPopulation(),
                        new ConsumptionProfile(mapDemandResources(oldProfile.getMinimalDemand())),
                        new ConsumptionProfile(mapDemandResources(oldProfile.getOptimalDemand()))))
            .toList();

    return new MissionManifest(
        missionPlanId,
        oldPlan.getMissionDurationSols(),
        0,
        oldPlan.getMaxStartingWeight(),
        mappedCrew,
        List.of(),
        List.of());
  }

  @Override
  public List<Module> getMissionModules(int missionPlanId) {
    miasi.backend.domains.configuration.missionPlan.MissionPlan oldPlan =
        confService.getMissionPlan(missionPlanId);

    if (oldPlan == null) {
      throw new RuntimeException("Nie znaleziono planu misji o ID: " + missionPlanId);
    }

    if (oldPlan.getModules() == null) {
      return List.of();
    }

    return oldPlan.getModules().stream()
        .map(
            oldMod ->
                Module.builder()
                    .id(moduleId(oldMod))
                    .name(oldMod.getName())
                    .weight(oldMod.getWeight())
                    .minCount(0)
                    .maxCount(1)
                    .production(mapResources(oldMod.getResourceProduction()))
                    .consumption(mapResources(oldMod.getResourceConsumption()))
                    .status(ModuleState.ACTIVE)
                    .efficiency(1.0f)
                    .build())
        .toList();
  }

  private List<Resource> mapResources(
      List<miasi.backend.domains.configuration.Resources> oldResources) {
    if (oldResources == null) {
      return List.of();
    }

    return oldResources.stream()
        .map(
            oldRes ->
                new Resource(
                    ResourceType.valueOf(oldRes.getResourceType().name()), oldRes.getQuantity()))
        .toList();
  }

  private String moduleId(miasi.backend.domains.configuration.modules.Module module) {
    if (module.getName() == null || module.getName().isBlank()) {
      return "module";
    }
    return module.getName().trim().toLowerCase().replaceAll("[^a-z0-9]+", "-");
  }

  private List<Resource> mapDemandResources(
      java.util.Map<miasi.backend.domains.configuration.enums.ResourceType, Float> demand) {
    if (demand == null) {
      return List.of();
    }

    return demand.entrySet().stream()
        .map(entry -> new Resource(ResourceType.valueOf(entry.getKey().name()), entry.getValue()))
        .toList();
  }
}
