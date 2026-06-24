package miasi.backend.domains.analysis.domain._payload;

import lombok.Value;
import miasi.backend.domains.analysis.domain._payload.PayloadOptimizationSession;

@Value
public class PayloadOptimizationCompletedEvent {
  PayloadOptimizationSession session;
}