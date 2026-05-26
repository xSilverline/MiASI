package schedule.domain;

import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import schedule.domain.enums.DifficultyLevel;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
public class ThreatDictionary {
  List<ThreatDefinition> definitions;

  public List<ThreatDefinition> findForDifficulty(DifficultyLevel difficulty) {
    return null;
  }
}
