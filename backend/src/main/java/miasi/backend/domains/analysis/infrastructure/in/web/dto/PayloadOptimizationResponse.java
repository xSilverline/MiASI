package miasi.backend.domains.analysis.infrastructure.in.web.dto;

import miasi.backend.domains.analysis.domain._payload.OptimalConfiguration;
import miasi.backend.domains.analysis.domain._payload.PayloadOptimizationSession;

public record PayloadOptimizationResponse(String sessionId, OptimalConfiguration configuration) {
  public static PayloadOptimizationResponse fromDomain(PayloadOptimizationSession session) {
    return new PayloadOptimizationResponse(session.getId(), session.getConfiguration());
  }
}
