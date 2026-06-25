package miasi.backend.domains.analysis.application.port.out;

import miasi.backend.domains.analysis.domain._payload.PayloadOptimizationSession;

public interface IPayloadSessionRepositoryPort {
  void save(PayloadOptimizationSession session);
}
