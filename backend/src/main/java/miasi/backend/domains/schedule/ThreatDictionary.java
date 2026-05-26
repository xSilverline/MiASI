package miasi.backend.domains.schedule;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import miasi.backend.enums.DifficultyLevel;

import java.util.List;

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
