package miasi.backend.configuration.infrastructure.in.web.dto;

import miasi.backend.configuration.domain.model.MissionPlan;
import miasi.backend.configuration.domain.model.Module;
import miasi.backend.configuration.domain.model.ModuleType;
import miasi.backend.configuration.domain.model.Resources;
import miasi.backend.configuration.domain.model.SexProfile;

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
