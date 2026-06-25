package miasi.backend.domains.schedule;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import miasi.backend.domains.schedule.enums.DifficultyLevel;
import miasi.backend.domains.schedule.enums.ThreatType;
import org.junit.jupiter.api.Test;

class ThreatDictionaryTest {

  @Test
  void findForDifficulty_shouldReturnDefinitionsMatchingDifficulty() {
    // Given
    ThreatDefinition easy = definition("oxygen", DifficultyLevel.LEVEL_I);
    ThreatDefinition hard = definition("power", DifficultyLevel.LEVEL_IV);
    ThreatDefinition secondEasy = definition("water", DifficultyLevel.LEVEL_I);
    ThreatDictionary dictionary = new ThreatDictionary(List.of(easy, hard, secondEasy));

    // When
    List<ThreatDefinition> result = dictionary.findForDifficulty(DifficultyLevel.LEVEL_I);

    // Then
    assertEquals(List.of(easy, secondEasy), result);
  }

  @Test
  void findForDifficulty_shouldReturnEmptyListWhenNoDefinitionsMatch() {
    // Given
    ThreatDictionary dictionary =
        new ThreatDictionary(List.of(definition("oxygen", DifficultyLevel.LEVEL_I)));

    // When
    List<ThreatDefinition> result = dictionary.findForDifficulty(DifficultyLevel.LEVEL_V);

    // Then
    assertTrue(result.isEmpty());
  }

  @Test
  void findForDifficulty_shouldReturnEmptyListWhenDefinitionsAreNull() {
    // Given
    ThreatDictionary dictionary = new ThreatDictionary(null);

    // When
    List<ThreatDefinition> result = dictionary.findForDifficulty(DifficultyLevel.LEVEL_III);

    // Then
    assertTrue(result.isEmpty());
  }

  @Test
  void findForDifficulty_shouldThrowWhenDifficultyIsNull() {
    // Given
    ThreatDictionary dictionary =
        new ThreatDictionary(List.of(definition("oxygen", DifficultyLevel.LEVEL_I)));

    // When + Then
    assertThrows(IllegalArgumentException.class, () -> dictionary.findForDifficulty(null));
  }

  private ThreatDefinition definition(String affectedElement, DifficultyLevel difficulty) {
    return new ThreatDefinition(
        ThreatType.DUST_STORM,
        difficulty,
        affectedElement,
        "reduced operational capacity",
        1.0,
        3.0,
        "days");
  }
}
