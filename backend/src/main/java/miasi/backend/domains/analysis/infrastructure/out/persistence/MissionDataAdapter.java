package miasi.backend.domains.analysis.infrastructure.out.persistence;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import miasi.backend.api.config.ConfService;
import miasi.backend.domains.analysis.application.port.out.IMissionDataProviderPort;
import miasi.backend.domains.analysis.domain.core.MissionManifest;
import miasi.backend.domains.analysis.domain.core.Resource;
import miasi.backend.domains.analysis.domain.core.ResourceType;
import miasi.backend.domains.analysis.domain.modules.Module;
import miasi.backend.domains.analysis.domain.modules.ModuleState;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MissionDataAdapter implements IMissionDataProviderPort {

  private final ConfService confService;

  @Override
  public MissionManifest getMissionManifest(int missionPlanId) {
    miasi.backend.domains.configuration.missionPlan.MissionPlan oldPlan = confService.getMissionPlan(
        missionPlanId);

    if (oldPlan == null) {
      throw new RuntimeException("Nie znaleziono planu misji o ID: " + missionPlanId);
    }

    List<miasi.backend.domains.analysis.domain.crew.CrewGroup> mappedCrew = oldPlan.getCrew()
        .stream()
        .map(oldProfile -> new miasi.backend.domains.analysis.domain.crew.CrewGroup(
            "Załoga standardowa",
            1,
            new miasi.backend.domains.analysis.domain.crew.ConsumptionProfile(List.of()),
            new miasi.backend.domains.analysis.domain.crew.ConsumptionProfile(List.of())
        ))
        .toList();

    return new MissionManifest(
        missionPlanId,
        oldPlan.getMissionDurationSols(),
        0,
        oldPlan.getMaxStartingWeight(),
        mappedCrew,
        List.of(),
        List.of()
    );
  }

  @Override
  public List<Module> getModuleCatalog() {
    List<miasi.backend.domains.configuration.modules.Module> oldCatalog = confService.getModuleCatalog();

    return oldCatalog.stream()
        .map(oldMod -> Module.builder()
            .id(UUID.randomUUID().toString())
            .name(oldMod.getName())
            .weight(oldMod.getWeight())
            .minCount(0)
            .production(mapResources(oldMod.getResourceProduction()))
            .consumption(mapResources(oldMod.getResourceConsumption()))
            .status(ModuleState.ACTIVE)
            .efficiency(1.0f)
            .build()
        )
        .collect(Collectors.toList());
  }

  private List<Resource> mapResources(
      List<miasi.backend.domains.configuration.Resources> oldResources) {
    if (oldResources == null) {
      return List.of();
    }

    return oldResources.stream()
        .map(oldRes -> new Resource(
            ResourceType.valueOf(oldRes.getResourceType().name()),
            oldRes.getQuantity()
        ))
        .toList();
  }
}