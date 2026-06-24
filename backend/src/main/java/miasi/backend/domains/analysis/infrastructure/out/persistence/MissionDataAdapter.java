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

    // 2. Mapowanie starej załogi na nową pancerną klasę CrewGroup
    List<miasi.backend.domains.analysis.domain.crew.CrewGroup> mappedCrew = oldPlan.getCrew()
        .stream()
        .map(oldProfile -> new miasi.backend.domains.analysis.domain.crew.CrewGroup(

            // 1. Nazwa grupy (np. wyciągnięta z typu/płci ze starego obiektu)
            "Załoga standardowa", // Zmień na np. oldProfile.getSex().name() jeśli masz taką metodę

            // 2. Ilość osób
            1, // Zmień na np. oldProfile.getCount() lub zostaw 1, jeśli każdy profil to 1 osoba

            // 3. Profil MINIMALNY (Tworzymy obiekt ConsumptionProfile na podstawie Twojego konstruktora)
            // Zakładam, że ConsumptionProfile przyjmuje List<Resource>
            new miasi.backend.domains.analysis.domain.crew.ConsumptionProfile(List.of(
                // Wstaw tu minimalne dawki na przetrwanie (np. 50% normy)
            )),

            // 4. Profil OPTYMALNY
            new miasi.backend.domains.analysis.domain.crew.ConsumptionProfile(List.of(
                // Wstaw tu optymalne dawki (np. 100% normy, może wyciągnięte ze starej bazy)
            ))
        ))
        .toList();

    // 3. Budujemy nowy Manifest Misji
    return new MissionManifest(
        missionPlanId,
        oldPlan.getMissionDurationSols(),
        0,
        oldPlan.getMaxStartingWeight(),
        mappedCrew,                       // <-- Nasza nowa, 4-argumentowa lista załogi!
        List.of(),
        List.of()
    );
  }

  @Override
  public List<Module> getModuleCatalog() {
    List<miasi.backend.domains.configuration.modules.Module> oldCatalog = confService.getModuleCatalog();

    return oldCatalog.stream()
        .map(oldMod -> new Module(
            UUID.randomUUID().toString(),
            oldMod.getName(),
            oldMod.getWeight(),
            0,
            null,
            mapResources(oldMod.getResourceProduction()),
            mapResources(oldMod.getResourceConsumption()),
            ModuleState.ACTIVE,
            1.0f
        ))
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