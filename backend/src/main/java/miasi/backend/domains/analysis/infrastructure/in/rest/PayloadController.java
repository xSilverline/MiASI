package miasi.backend.domains.analysis.infrastructure.in.rest;

import lombok.RequiredArgsConstructor;
import miasi.backend.domains.analysis.application.port.in.IOptimizePayloadUseCase;
import miasi.backend.domains.analysis.application.port.in.OptimizePayloadCommand;
import miasi.backend.domains.analysis.domain._payload.PayloadOptimizationSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analysis/payload")
@RequiredArgsConstructor
public class PayloadController {

  private final IOptimizePayloadUseCase optimizePayloadUseCase;

  @PostMapping("/optimize")
  public ResponseEntity<PayloadOptimizationSession> optimizePayload(
      @RequestBody OptimizePayloadCommand command) {

    PayloadOptimizationSession session = optimizePayloadUseCase.optimize(command);

    return ResponseEntity.ok(session);
  }
}