package miasi.backend.domains.analysis.infrastructure.in.web;

import lombok.RequiredArgsConstructor;
import miasi.backend.domains.analysis.application.port.in.IOptimizePayloadUseCase;
import miasi.backend.domains.analysis.application.port.in.IRunNominalSimulationUseCase;
import miasi.backend.domains.analysis.application.port.in.IRunScenariosSimulationUseCase;
import miasi.backend.domains.analysis.application.port.in.OptimizePayloadCommand;
import miasi.backend.domains.analysis.application.port.in.RunNominalSimulationCommand;
import miasi.backend.domains.analysis.application.port.in.RunScenariosSimulationCommand;
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
@CrossOrigin(origins = "http://localhost:*")
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

    var command = new RunNominalSimulationCommand(
        request.payloadSessionId(),
        request.customizedModules(),
        request.customizedSupplies()
    );
    var session = runNominalSimulationUseCase.simulate(command);

    return ResponseEntity.ok(NominalSimulationResponse.fromDomain(session));
  }

  @PostMapping("/simulate/scenarios")
  public ResponseEntity<ScenariosSimulationResponse> simulateScenarios(
      @RequestBody RunScenariosSimulationRequest request) {

    var command = new RunScenariosSimulationCommand(
        request.nominalSessionId(),
        request.scheduleId()
    );
    var session = runScenariosSimulationUseCase.simulate(command);

    return ResponseEntity.ok(ScenariosSimulationResponse.fromDomain(session));
  }
}