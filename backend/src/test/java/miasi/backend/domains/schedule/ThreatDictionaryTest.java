package miasi.backend.domains.schedule;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import miasi.backend.enums.DifficultyLevel;
import miasi.backend.enums.ThreatType;
import org.junit.jupiter.api.Test;

class ThreatDictionaryTest {

  @Test
  void findForDifficulty_shouldReturnDefinitionsMatchingDifficulty() {
    ThreatDefinition easy = definition("oxygen", DifficultyLevel.LEVEL_I);
    ThreatDefinition hard = definition("power", DifficultyLevel.LEVEL_IV);
    ThreatDefinition secondEasy = definition("water", DifficultyLevel.LEVEL_I);
    ThreatDictionary dictionary = new ThreatDictionary(List.of(easy, hard, secondEasy));

    List<ThreatDefinition> result = dictionary.findForDifficulty(DifficultyLevel.LEVEL_I);

    assertEquals(List.of(easy, secondEasy), result);
  }

  @Test
  void findForDifficulty_shouldReturnEmptyListWhenNoDefinitionsMatch() {
    ThreatDictionary dictionary =
        new ThreatDictionary(List.of(definition("oxygen", DifficultyLevel.LEVEL_I)));

    List<ThreatDefinition> result = dictionary.findForDifficulty(DifficultyLevel.LEVEL_V);

    assertTrue(result.isEmpty());
  }

  @Test
  void findForDifficulty_shouldReturnEmptyListWhenDefinitionsAreNull() {
    ThreatDictionary dictionary = new ThreatDictionary(null);

    List<ThreatDefinition> result = dictionary.findForDifficulty(DifficultyLevel.LEVEL_III);

    assertTrue(result.isEmpty());
  }

  @Test
  void findForDifficulty_shouldThrowWhenDifficultyIsNull() {
    ThreatDictionary dictionary =
        new ThreatDictionary(List.of(definition("oxygen", DifficultyLevel.LEVEL_I)));

    assertThrows(IllegalArgumentException.class, () -> dictionary.findForDifficulty(null));
  }

  private ThreatDefinition definition(String affectedElement, DifficultyLevel difficulty) {
    return new ThreatDefinition(
        ThreatType.DUST_STORM, difficulty, affectedElement, 1.0, 3.0, "days");
  }
}
