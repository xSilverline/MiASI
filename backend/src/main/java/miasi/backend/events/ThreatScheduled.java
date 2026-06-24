package miasi.backend.events;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import miasi.backend.domains.schedule.enums.ThreatType;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
public class ThreatScheduled {
  String scheduleId;
  int sol;
  ThreatType threatType;
  String affectedElement;
  String consequence;
  double impactValue;
  String impactUnit;
}
