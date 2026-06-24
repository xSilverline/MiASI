package miasi.backend.domains.analysis.domain._payload;

import java.time.LocalDateTime;
import lombok.Value;
import miasi.backend.domains.analysis.domain.core.MissionManifest;

@Value
public class PayloadOptimizationSession {

  String id;
  MissionManifest inputManifest;
  OptimalConfiguration configuration;
  LocalDateTime createdAt;
}
