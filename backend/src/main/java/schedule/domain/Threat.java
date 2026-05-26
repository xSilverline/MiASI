package schedule.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import schedule.domain.enums.ThreatType;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
public class Threat extends ScheduledEvent {
  ThreatType threatType;
  String affectedElement;
  double impactValue;
  String impactUnit;
}
