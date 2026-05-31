package miasi.backend.domains.configuration.other;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import miasi.backend.domains.configuration.MissionPlan;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
public class MissionPlanCreatedEvent {
  String missionPlanId;
  MissionPlan missionPlan;
}
