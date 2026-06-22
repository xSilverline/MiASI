package miasi.backend.domains.schedule;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import miasi.backend.schedule.domain.DifficultyLevel;
import miasi.backend.schedule.domain.EventType;
import miasi.backend.schedule.domain.ScenarioGenerationMode;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
public class ScenarioGenerator {
  String missionPlanId;
  ThreatDictionary threatDictionary;
  Random random;

  public ScenarioGenerator(String missionPlanId) {
    this(missionPlanId, new ThreatDictionary(List.of()), new Random());
  }

  public ScenarioDraft generate(String planId, int durationSols, DifficultyLevel difficulty) {
    if (planId == null || planId.isBlank()) {
      throw new IllegalArgumentException("Mission plan id is required");
    }
    if (durationSols < 1) {
      throw new IllegalArgumentException("Mission duration must be at least 1 sol");
    }
    if (difficulty == null) {
      throw new IllegalArgumentException("Difficulty level is required");
    }

    this.missionPlanId = planId;
    Random effectiveRandom = random == null ? new Random() : random;
    ThreatDictionary effectiveDictionary =
        threatDictionary == null ? new ThreatDictionary(List.of()) : threatDictionary;

    List<ScheduledEvent> events = new ArrayList<>();
    for (ThreatDefinition definition : effectiveDictionary.findForDifficulty(difficulty)) {
      events.add(buildThreat(definition, durationSols, effectiveRandom));
    }
    events.addAll(buildSupplyDeliveries(durationSols));

    return new ScenarioDraft(
        UUID.randomUUID().toString(),
        planId,
        durationSols,
        ScenarioGenerationMode.AUTOMATIC,
        difficulty,
        events);
  }

  private Threat buildThreat(
      ThreatDefinition definition, int durationSols, Random effectiveRandom) {
    validateThreatDefinition(definition);

    double impactValue =
        definition.getMinImpactValue()
            + (definition.getMaxImpactValue() - definition.getMinImpactValue())
                * effectiveRandom.nextDouble();
    int threatDuration =
        randomIntBetween(
            (int) Math.ceil(definition.getMinImpactValue()),
            (int) Math.floor(definition.getMaxImpactValue()),
            effectiveRandom);

    Threat threat =
        new Threat(
            definition.getType(),
            definition.getAffectedElement(),
            impactValue,
            threatDuration,
            definition.getImpactUnit());
    threat.setId(UUID.randomUUID().toString());
    threat.setType(EventType.THREAT);
    threat.setSol(randomIntBetween(1, durationSols, effectiveRandom));
    threat.setDescription(
        "Threat "
            + definition.getType()
            + " affects "
            + definition.getAffectedElement()
            + " for "
            + threatDuration
            + " sols");
    return threat;
  }

  private void validateThreatDefinition(ThreatDefinition definition) {
    if (definition == null) {
      throw new IllegalArgumentException("Threat definition is required");
    }
    if (definition.getMinImpactValue() > definition.getMaxImpactValue()) {
      throw new IllegalArgumentException("Threat min impact cannot exceed max impact");
    }
  }

  private int randomIntBetween(int min, int max, Random effectiveRandom) {
    if (min > max) {
      return min;
    }
    return min + effectiveRandom.nextInt(max - min + 1);
  }

  private List<ScheduledEvent> buildSupplyDeliveries(int durationSols) {
    List<ScheduledEvent> deliveries = new ArrayList<>();
    for (int sol = 1; sol <= durationSols; sol += 30) {
      SupplyDelivery delivery = new SupplyDelivery(new DeliveryContent(List.of(), 0.0));
      delivery.setId(UUID.randomUUID().toString());
      delivery.setType(EventType.SUPPLY_DELIVERY);
      delivery.setSol(sol);
      delivery.setDescription("Scheduled supply delivery");
      deliveries.add(delivery);
    }
    return deliveries;
  }
}
