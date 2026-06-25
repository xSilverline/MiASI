package miasi.backend.domains.analysis.domain._payload;

import lombok.Value;

@Value
public class PayloadOptimizationCompletedEvent {
  PayloadOptimizationSession session;
}
