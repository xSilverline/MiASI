package miasi.backend.domains.schedule;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Random;
import miasi.backend.enums.DifficultyLevel;
import miasi.backend.enums.EventType;
import miasi.backend.enums.ScenarioGenerationMode;
import miasi.backend.enums.ThreatType;
import org.junit.jupiter.api.Test;

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
}
