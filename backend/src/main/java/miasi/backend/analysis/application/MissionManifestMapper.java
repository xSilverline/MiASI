package miasi.backend.analysis.application;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import miasi.backend.domains.analysis.types.core.Resource;
import miasi.backend.domains.analysis.types.crew.ConsumptionProfile;
import miasi.backend.domains.analysis.types.crew.CrewGroup;
import miasi.backend.domains.analysis.types.input.MissionManifest;
import miasi.backend.domains.analysis.types.modules.Module;
import miasi.backend.domains.analysis.types.schedule.Delivery;
import miasi.backend.domains.analysis.types.schedule.ImpactTarget;
import miasi.backend.domains.analysis.types.schedule.ImpactType;
import miasi.backend.domains.configuration.missionPlan.MissionPlan;
import miasi.backend.domains.configuration.modules.ModuleCatalog;
import miasi.backend.domains.configuration.other.Resources;
import miasi.backend.domains.configuration.other.SexProfile;
import miasi.backend.domains.schedule.DeliveryItem;
import miasi.backend.domains.schedule.MissionSchedule;
import miasi.backend.domains.schedule.ScheduledEvent;
import miasi.backend.domains.schedule.SupplyDelivery;
import miasi.backend.schedule.domain.DeliveryItemType;
import miasi.backend.schedule.domain.ThreatType;
import miasi.backend.sharedkernel.model.ModuleState;
import miasi.backend.sharedkernel.model.ResourceType;

public class MissionManifestMapper {

  public MissionManifest toManifest(
      UUID manifestId,
      MissionPlan missionPlan,
      ModuleCatalog moduleCatalog,
      MissionSchedule schedule) {
    if (manifestId == null) {
      throw new IllegalArgumentException("Manifest id is required");
    }
    if (missionPlan == null) {
      throw new IllegalArgumentException("Mission plan is required");
    }
    if (schedule == null) {
      throw new IllegalArgumentException("Mission schedule is required");
    }

    List<Module> catalog = mapCatalog(moduleCatalog, missionPlan);
    return new MissionManifest(
        manifestId,
        resolveDuration(missionPlan, schedule),
        0,
        missionPlan.getMaxStartingWeight(),
        mapCrew(missionPlan.getCrew()),
        catalog,
        mapDeliveries(schedule.getEvents(), catalog),
        mapThreats(schedule.getEvents()));
  }

  private int resolveDuration(MissionPlan missionPlan, MissionSchedule schedule) {
    return schedule.getDurationSols() > 0
        ? schedule.getDurationSols()
        : missionPlan.getMissionDurationSols();
  }

  private List<CrewGroup> mapCrew(List<SexProfile> profiles) {
    if (profiles == null) {
      return List.of();
    }

    return profiles.stream()
        .map(
            profile ->
                new CrewGroup(
                    profile.getName(),
                    profile.getPopulation(),
                    new ConsumptionProfile(mapDemand(profile.getMinimalDemand())),
                    new ConsumptionProfile(mapDemand(profile.getOptimalDemand()))))
        .toList();
  }

  private List<Resource> mapDemand(Map<ResourceType, Float> demand) {
    if (demand == null) {
      return List.of();
    }

    return demand.entrySet().stream()
        .map(entry -> new Resource(entry.getKey(), entry.getValue()))
        .toList();
  }

  private List<Module> mapCatalog(ModuleCatalog moduleCatalog, MissionPlan missionPlan) {
    List<miasi.backend.domains.configuration.modules.Module> sourceModules =
        moduleCatalog != null && moduleCatalog.moduleList() != null
            ? moduleCatalog.moduleList()
            : safeModules(missionPlan);
    Map<String, Integer> selectedCounts = selectedModuleCounts(missionPlan);

    return sourceModules.stream()
        .map(module -> mapModule(module, selectedCounts.getOrDefault(module.getName(), 0)))
        .toList();
  }

  private Module mapModule(
      miasi.backend.domains.configuration.modules.Module module, int selectedCount) {
    return new Module(
        module.getName(),
        module.getWeight(),
        selectedCount,
        null,
        mapResources(module.getType().getResourceProduction()),
        mapResources(module.getType().getResourceConsumption()),
        module.getStatus() == null ? ModuleState.ACTIVE : module.getStatus(),
        1.0f);
  }

  private List<Resource> mapResources(List<Resources> resources) {
    if (resources == null) {
      return List.of();
    }

    return resources.stream()
        .map(resource -> new Resource(resource.getResourceType(), resource.getQuantity()))
        .toList();
  }

  private Map<String, Integer> selectedModuleCounts(MissionPlan missionPlan) {
    Map<String, Integer> counts = new java.util.HashMap<>();
    for (miasi.backend.domains.configuration.modules.Module module : safeModules(missionPlan)) {
      counts.merge(module.getName(), 1, Integer::sum);
    }
    return counts;
  }

  private List<miasi.backend.domains.configuration.modules.Module> safeModules(
      MissionPlan missionPlan) {
    return missionPlan.getModules() == null ? List.of() : missionPlan.getModules();
  }

  private List<Delivery> mapDeliveries(List<ScheduledEvent> events, List<Module> catalog) {
    List<Delivery> deliveries = new ArrayList<>();
    for (ScheduledEvent event : safeEvents(events)) {
      if (event instanceof SupplyDelivery delivery) {
        deliveries.add(
            new Delivery(
                delivery.getSol(),
                mapDeliveryResources(delivery),
                mapDeliveryModules(delivery, catalog)));
      }
    }
    return deliveries;
  }

  private List<Resource> mapDeliveryResources(SupplyDelivery delivery) {
    Map<ResourceType, Float> resources = new EnumMap<>(ResourceType.class);
    for (DeliveryItem item : deliveryItems(delivery)) {
      if (item.getItemType() == DeliveryItemType.RESOURCE) {
        resources.merge(
            ResourceType.valueOf(item.getItemId()), (float) item.getQuantity(), Float::sum);
      }
    }
    return resources.entrySet().stream()
        .map(entry -> new Resource(entry.getKey(), entry.getValue()))
        .toList();
  }

  private List<Module> mapDeliveryModules(SupplyDelivery delivery, List<Module> catalog) {
    List<Module> modules = new ArrayList<>();
    for (DeliveryItem item : deliveryItems(delivery)) {
      if (item.getItemType() == DeliveryItemType.MODULE) {
        catalog.stream()
            .filter(module -> module.getName().equals(item.getItemId()))
            .findFirst()
            .map(Module::copy)
            .ifPresent(modules::add);
      }
    }
    return modules;
  }

  private List<DeliveryItem> deliveryItems(SupplyDelivery delivery) {
    if (delivery.getContent() == null || delivery.getContent().getItems() == null) {
      return List.of();
    }
    return delivery.getContent().getItems();
  }

  private List<miasi.backend.domains.analysis.types.schedule.Threat> mapThreats(
      List<ScheduledEvent> events) {
    List<miasi.backend.domains.analysis.types.schedule.Threat> threats = new ArrayList<>();
    for (ScheduledEvent event : safeEvents(events)) {
      if (event instanceof miasi.backend.domains.schedule.Threat threat) {
        threats.add(
            new miasi.backend.domains.analysis.types.schedule.Threat(
                threat.getSol(),
                threat.getDurationSols(),
                impactType(threat.getThreatType()),
                impactTarget(threat.getThreatType()),
                threat.getAffectedElement(),
                (float) threat.getImpactValue()));
      }
    }
    return threats;
  }

  private ImpactType impactType(ThreatType threatType) {
    return switch (threatType) {
      case RESOURCE_LOSS -> ImpactType.QUANTITY_CHANGE;
      case MODULE_FAILURE -> ImpactType.STATE_CHANGE;
      case DUST_STORM, PRODUCTION_DISRUPTION -> ImpactType.EFFICIENCY_CHANGE;
    };
  }

  private ImpactTarget impactTarget(ThreatType threatType) {
    return threatType == ThreatType.RESOURCE_LOSS ? ImpactTarget.RESOURCE : ImpactTarget.MODULE;
  }

  private List<ScheduledEvent> safeEvents(List<ScheduledEvent> events) {
    return events == null ? List.of() : events;
  }
}
