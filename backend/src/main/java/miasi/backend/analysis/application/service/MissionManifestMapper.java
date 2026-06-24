package miasi.backend.analysis.application.service;

import miasi.backend.analysis.domain.model.core.Resource;
import miasi.backend.analysis.domain.model.crew.ConsumptionProfile;
import miasi.backend.analysis.domain.model.crew.CrewGroup;
import miasi.backend.analysis.domain.model.input.MissionManifest;
import miasi.backend.analysis.domain.model.modules.Module;
import miasi.backend.analysis.domain.model.schedule.Delivery;
import miasi.backend.analysis.domain.model.schedule.ImpactTarget;
import miasi.backend.analysis.domain.model.schedule.ImpactType;
import miasi.backend.common.domain.model.ModuleState;
import miasi.backend.common.domain.model.ResourceType;
import miasi.backend.configuration.domain.model.MissionPlan;
import miasi.backend.configuration.domain.model.Resources;
import miasi.backend.configuration.domain.model.SexProfile;
import miasi.backend.schedule.domain.model.DeliveryItem;
import miasi.backend.schedule.domain.model.DeliveryItemType;
import miasi.backend.schedule.domain.model.MissionSchedule;
import miasi.backend.schedule.domain.model.ScheduledEvent;
import miasi.backend.schedule.domain.model.SupplyDelivery;
import miasi.backend.schedule.domain.model.ThreatType;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MissionManifestMapper {

  public MissionManifest toManifest(
      UUID manifestId,
      MissionPlan missionPlan,
      List<miasi.backend.configuration.domain.model.Module> moduleCatalog,
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
    List<Module> moduleList = mapCatalog(moduleCatalog, missionPlan);

    return new MissionManifest(
        manifestId,
        resolveDuration(missionPlan, schedule),
        0,
        missionPlan.getMaxStartingWeight(),
        mapCrew(missionPlan.getCrew()),
        moduleList,
        mapDeliveries(schedule.getEvents(), moduleList),
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

  private List<Module> mapCatalog(List<miasi.backend.configuration.domain.model.Module> moduleCatalog, MissionPlan missionPlan) {
    List<miasi.backend.configuration.domain.model.Module> sourceModules =
        moduleCatalog != null
            ? moduleCatalog
            : safeModules(missionPlan);
    Map<String, Integer> selectedCounts = selectedModuleCounts(missionPlan);

    return sourceModules.stream()
        .map(module -> mapModule(module, selectedCounts.getOrDefault(module.getName(), 0)))
        .toList();
  }

  private Module mapModule(
      miasi.backend.configuration.domain.model.Module module, int selectedCount) {
    return new Module(
        module.getName(),
        module.getWeight(),
        selectedCount,
        null,
        mapResources(module.getResourceProduction()),
        mapResources(module.getResourceConsumption()),
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
    for (miasi.backend.configuration.domain.model.Module module : safeModules(missionPlan)) {
      counts.merge(module.getName(), 1, Integer::sum);
    }
    return counts;
  }

  private List<miasi.backend.configuration.domain.model.Module> safeModules(
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

  private List<miasi.backend.analysis.domain.model.schedule.Threat> mapThreats(
      List<ScheduledEvent> events) {
    List<miasi.backend.analysis.domain.model.schedule.Threat> threats = new ArrayList<>();
    for (ScheduledEvent event : safeEvents(events)) {
      if (event instanceof miasi.backend.schedule.domain.model.Threat threat) {
        threats.add(
            new miasi.backend.analysis.domain.model.schedule.Threat(
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
