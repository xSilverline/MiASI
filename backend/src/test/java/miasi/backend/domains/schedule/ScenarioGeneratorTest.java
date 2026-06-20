package miasi.backend.domains.schedule;

import miasi.backend.enums.DifficultyLevel;
import miasi.backend.enums.EventType;
import miasi.backend.enums.ScenarioGenerationMode;
import miasi.backend.enums.ThreatType;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ScenarioGeneratorTest {

  @Test
  void generate_shouldCreateScenarioDraftWithThreatsForDifficultyAndDeliveries() {
    ThreatDefinition matching =
        new ThreatDefinition(
            ThreatType.DUST_STORM,
            DifficultyLevel.LEVEL_II,
            "solar-panels",
            2.0,
            5.0,
            "days");
    ThreatDefinition otherDifficulty =
        new ThreatDefinition(
            ThreatType.RESOURCE_LOSS,
            DifficultyLevel.LEVEL_V,
            "water",
            5.0,
            8.0,
            "kg");
    ScenarioGenerator generator =
        new ScenarioGenerator(
            "plan-1",
            new ThreatDictionary(List.of(matching, otherDifficulty)),
            new Random(0));

    ScenarioDraft draft = generator.generate("plan-1", 90, DifficultyLevel.LEVEL_II);

    assertEquals(ScenarioGenerationMode.AUTOMATIC, draft.getMode());
    assertEquals(DifficultyLevel.LEVEL_II, draft.getDifficulty());
    assertFalse(draft.getProposedEvents().isEmpty());
    assertEquals(1, draft.getProposedEvents().stream().filter(Threat.class::isInstance).count());
    assertEquals(
        3,
        draft.getProposedEvents().stream()
            .filter(event -> event.getType() == EventType.SUPPLY_DELIVERY)
            .count());
  }

  @Test
  void generate_shouldKeepGeneratedValuesWithinMissionAndDefinitionRanges() {
    ThreatDefinition definition =
        new ThreatDefinition(
            ThreatType.MODULE_FAILURE,
            DifficultyLevel.LEVEL_III,
            "habitat",
            2.0,
            4.0,
            "days");
    ScenarioGenerator generator =
        new ScenarioGenerator(
            "plan-1", new ThreatDictionary(List.of(definition)), new Random(1));

    ScenarioDraft draft = generator.generate("plan-1", 60, DifficultyLevel.LEVEL_III);

    Threat threat =
        draft.getProposedEvents().stream()
            .filter(Threat.class::isInstance)
            .map(Threat.class::cast)
            .findFirst()
            .orElseThrow();

    assertTrue(threat.getSol() >= 1);
    assertTrue(threat.getSol() <= 60);
    assertTrue(threat.getImpactValue() >= definition.getMinImpactValue());
    assertTrue(threat.getImpactValue() <= definition.getMaxImpactValue());
    assertTrue(threat.getDurationSols() >= 2);
    assertTrue(threat.getDurationSols() <= 4);
  }

  @Test
  void validateThreatDefinition_exceptionsThrowTest() throws NoSuchMethodException {
    // Given
    ThreatDefinition valid = new ThreatDefinition(
        ThreatType.DUST_STORM, DifficultyLevel.LEVEL_I, "test", 2.0, 5.0, "days");
    ThreatDefinition invalid = new ThreatDefinition(
        ThreatType.DUST_STORM, DifficultyLevel.LEVEL_I, "test", 10.0, 5.0, "days");
    ScenarioGenerator generator = new ScenarioGenerator(
        "plan-1", new ThreatDictionary(List.of()), new Random(0));
    java.lang.reflect.Method method = ScenarioGenerator.class.getDeclaredMethod(
        "validateThreatDefinition", ThreatDefinition.class);
    method.setAccessible(true);

    // When + Then
    assertThrows(InvocationTargetException.class, () -> {
      method.invoke(generator, (Object) null);
    });
    assertThrows(InvocationTargetException.class, () -> {
      method.invoke(generator, invalid);
    });
    assertDoesNotThrow(() -> {
      method.invoke(generator, valid);
    });
  }

  @Test
  void generate_exceptionsThrowTest() {
    // Given
    String planId = "0";
    int durationSols = 9;
    DifficultyLevel difficulty = DifficultyLevel.LEVEL_I;
    ScenarioGenerator generator = new ScenarioGenerator(
        "plan-1", new ThreatDictionary(List.of()), new Random(0));

    // When + Then
    assertThrows(IllegalArgumentException.class, () ->
        generator.generate(null, durationSols, difficulty));
    assertThrows(IllegalArgumentException.class, () ->
        generator.generate(null, 0, difficulty));
    assertThrows(IllegalArgumentException.class, () ->
        generator.generate(planId, durationSols, null));
    assertDoesNotThrow(() -> {
      generator.generate(planId, durationSols, difficulty);
    });
  }


  @Test
  void constructor_shouldSetDefaultValues() {
    // Given
    String planId = "0";
    int durationSols = 9;
    DifficultyLevel difficulty = DifficultyLevel.LEVEL_I;

    // When
    ScenarioGenerator generator = new ScenarioGenerator("0");

    // Then
    assertNotNull(generator.getThreatDictionary());
    assertNotNull(generator.getRandom());
    assertDoesNotThrow(() -> {
      generator.generate(planId, durationSols, difficulty);
    });
  }
}
