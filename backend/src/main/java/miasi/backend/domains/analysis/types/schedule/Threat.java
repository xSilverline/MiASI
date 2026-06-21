package miasi.backend.domains.analysis.types.schedule;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import miasi.backend.enums.ImpactTarget;
import miasi.backend.enums.ImpactType;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
public class Threat {
  int sol;
  int durationSols;
  ImpactType type;
  ImpactTarget target;
  String targetIdentifier; // np. nazwa modułu ("Farma") lub typ surowca
  float impactValue;
}