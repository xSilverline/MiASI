package miasi.backend.schedule.domain.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
public class Threat extends ScheduledEvent {
  ThreatType threatType;
  String affectedElement;
  double impactValue;
  int durationSols;
  String impactUnit;
}
