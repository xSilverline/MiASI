package miasi.backend.domains.analysis.infrastructure.in.web.dto;

import java.util.List;

public record RunNominalSimulationRequest(
    String payloadSessionId,
    List<ModuleDto> customizedModules,
    List<SupplyDto> customizedSupplies
) {

  public record ModuleDto(
      String name,
      String status,
      String category,
      float weight,
      List<ResourceDto> resourceConsumption,
      List<ResourceDto> resourceProduction
  ) {

  }

  public record SupplyDto(
      String type,
      float amount,
      float weight
  ) {

  }

  public record ResourceDto(
      String resourceType,
      float quantity
  ) {

  }
}