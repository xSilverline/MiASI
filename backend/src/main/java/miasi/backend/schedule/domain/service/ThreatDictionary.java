package miasi.backend.schedule.domain.service;

import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import miasi.backend.schedule.domain.model.DifficultyLevel;
import miasi.backend.schedule.domain.model.ThreatDefinition;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
public class ThreatDictionary {
  List<ThreatDefinition> definitions;

  public List<ThreatDefinition> findForDifficulty(DifficultyLevel difficulty) {
    if (difficulty == null) {
      throw new IllegalArgumentException("Difficulty level is required");
    }
    if (definitions == null) {
      return List.of();
    }

    return definitions.stream()
        .filter(definition -> difficulty.equals(definition.getDifficulty()))
        .toList();
  }
}
