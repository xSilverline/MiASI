package miasi.backend.domains.schedule;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import miasi.backend.schedule.domain.DifficultyLevel;
import miasi.backend.schedule.domain.ThreatType;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
public class ThreatDefinition {
  ThreatType type;
  DifficultyLevel difficulty;
  String affectedElement;
  double minImpactValue;
  double maxImpactValue;
  String impactUnit;
}
