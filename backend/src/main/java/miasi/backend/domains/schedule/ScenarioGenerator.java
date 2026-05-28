package miasi.backend.domains.schedule;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import miasi.backend.enums.DifficultyLevel;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
public class ScenarioGenerator {
  String missionPlanId;

  public ScenarioDraft generate(String planId, int durationSols, DifficultyLevel difficulty) {
    return null;
  }
}
