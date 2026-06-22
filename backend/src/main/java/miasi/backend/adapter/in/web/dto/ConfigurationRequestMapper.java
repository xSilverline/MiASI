package miasi.backend.adapter.in.web.dto;

import miasi.backend.domains.configuration.missionPlan.MissionPlan;
import miasi.backend.domains.configuration.modules.Module;
import miasi.backend.domains.configuration.modules.ModuleType;
import miasi.backend.domains.configuration.other.Resources;
import miasi.backend.domains.configuration.other.SexProfile;

public final class ConfigurationRequestMapper {

  private ConfigurationRequestMapper() {}

  public static MissionPlan toDomain(MissionPlanRequest request) {
    return new MissionPlan(
        request.crew().stream().map(ConfigurationRequestMapper::toDomain).toList(),
        request.missionDurationSols(),
        request.startingResources().stream().map(ConfigurationRequestMapper::toDomain).toList(),
        request.modules().stream().map(ConfigurationRequestMapper::toDomain).toList(),
        request.maxStartingWeight());
  }

  public static Module toDomain(ModuleRequest request) {
    return new Module(request.name(), request.status(), toDomain(request.type()), request.weight());
  }

  public static ModuleType toDomain(ModuleTypeRequest request) {
    return new ModuleType(
        request.name(),
        request.resourceConsumption().stream().map(ConfigurationRequestMapper::toDomain).toList(),
        request.resourceProduction().stream().map(ConfigurationRequestMapper::toDomain).toList());
  }

  private static SexProfile toDomain(SexProfileRequest request) {
    return new SexProfile(
        request.name(), request.population(), request.optimalDemand(), request.minimalDemand());
  }

  private static Resources toDomain(ResourceRequest request) {
    return new Resources(request.resourceType(), request.quantity());
  }
}
