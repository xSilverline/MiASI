package miasi.backend.domains.analysis.application.port.in;

import miasi.backend.domains.analysis.domain._payload.PayloadOptimizationSession;

public interface IOptimizePayloadUseCase {

  PayloadOptimizationSession optimize(OptimizePayloadCommand command);
}
