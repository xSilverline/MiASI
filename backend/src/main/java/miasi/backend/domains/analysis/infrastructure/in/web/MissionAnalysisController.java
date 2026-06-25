package miasi.backend.domains.analysis.infrastructure.in.web;

import java.util.List;
import lombok.RequiredArgsConstructor;
import miasi.backend.domains.analysis.application.port.in.IOptimizePayloadUseCase;
import miasi.backend.domains.analysis.application.port.in.IRunNominalSimulationUseCase;
import miasi.backend.domains.analysis.application.port.in.IRunScenariosSimulationUseCase;
import miasi.backend.domains.analysis.application.port.in.OptimizePayloadCommand;
import miasi.backend.domains.analysis.application.port.in.RunNominalSimulationCommand;
import miasi.backend.domains.analysis.application.port.in.RunScenariosSimulationCommand;
import miasi.backend.domains.analysis.domain.core.Resource;
import miasi.backend.domains.analysis.domain.core.ResourceType;
import miasi.backend.domains.analysis.domain.modules.Module;
import miasi.backend.domains.analysis.domain.modules.ModuleState;
import miasi.backend.domains.analysis.infrastructure.in.web.dto.NominalSimulationResponse;
import miasi.backend.domains.analysis.infrastructure.in.web.dto.PayloadOptimizationRequest;
import miasi.backend.domains.analysis.infrastructure.in.web.dto.PayloadOptimizationResponse;
import miasi.backend.domains.analysis.infrastructure.in.web.dto.RunNominalSimulationRequest;
import miasi.backend.domains.analysis.infrastructure.in.web.dto.RunScenariosSimulationRequest;
import miasi.backend.domains.analysis.infrastructure.in.web.dto.ScenariosSimulationResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analysis")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class MissionAnalysisController {

  private final IOptimizePayloadUseCase optimizePayloadUseCase;
  private final IRunNominalSimulationUseCase runNominalSimulationUseCase;
  private final IRunScenariosSimulationUseCase runScenariosSimulationUseCase;

  @PostMapping("/payload/optimize")
  public ResponseEntity<PayloadOptimizationResponse> optimizePayload(
      @RequestBody PayloadOptimizationRequest request) {

    var command = new OptimizePayloadCommand(request.missionPlanId());
    var session = optimizePayloadUseCase.optimize(command);

    return ResponseEntity.ok(PayloadOptimizationResponse.fromDomain(session));
  }

  @PostMapping("/simulate/nominal")
  public ResponseEntity<NominalSimulationResponse> simulateNominal(
      @RequestBody RunNominalSimulationRequest request) {

    List<RunNominalSimulationRequest.ModuleDto> customizedModules =
        request.customizedModules() == null ? List.of() : request.customizedModules();
    List<RunNominalSimulationRequest.SupplyDto> customizedSupplies =
        request.customizedSupplies() == null ? List.of() : request.customizedSupplies();

    List<Module> domainModules =
        customizedModules.stream()
            .map(
                dto ->
                    Module.builder()
                        .name(dto.name())
                        .weight(dto.weight())
                        .status(ModuleState.valueOf(dto.status()))
                        .consumption(
                            safeResources(dto.resourceConsumption()).stream()
                                .map(
                                    r ->
                                        new Resource(
                                            ResourceType.valueOf(r.resourceType()), r.quantity()))
                                .toList())
                        .production(
                            safeResources(dto.resourceProduction()).stream()
                                .map(
                                    r ->
                                        new Resource(
                                            ResourceType.valueOf(r.resourceType()), r.quantity()))
                                .toList())
                        .build())
            .toList();

    List<Resource> domainSupplies =
        customizedSupplies.stream()
            .map(dto -> new Resource(ResourceType.valueOf(dto.type()), dto.amount()))
            .toList();

    var command =
        new RunNominalSimulationCommand(request.payloadSessionId(), domainModules, domainSupplies);

    var session = runNominalSimulationUseCase.simulate(command);

    return ResponseEntity.ok(NominalSimulationResponse.fromDomain(session));
  }

  @PostMapping("/simulate/scenarios")
  public ResponseEntity<ScenariosSimulationResponse> simulateScenarios(
      @RequestBody RunScenariosSimulationRequest request) {

    var command =
        new RunScenariosSimulationCommand(request.nominalSessionId(), request.scheduleId());
    var session = runScenariosSimulationUseCase.simulate(command);

    return ResponseEntity.ok(ScenariosSimulationResponse.fromDomain(session));
  }

  private List<RunNominalSimulationRequest.ResourceDto> safeResources(
      List<RunNominalSimulationRequest.ResourceDto> resources) {
    return resources == null ? List.of() : resources;
  }
}
