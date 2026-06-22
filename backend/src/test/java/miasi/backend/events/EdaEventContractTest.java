package miasi.backend.events;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition;
import miasi.backend.sharedkernel.model.ModuleState;
import org.junit.jupiter.api.Test;

class EdaEventContractTest {

  @Test
  void missionPlanCreatedShouldHaveEnvelopeAndNoAggregatePayload() {
    MissionPlanCreated event = MissionPlanCreated.create(12);

    assertEquals("MissionPlanCreated", event.eventType());
    assertEquals(12, event.missionPlanId());
    assertEnvelope(event.envelope(), "12");
  }

  @Test
  void missionPlanUpdatedShouldHaveSeparateEventType() {
    MissionPlanUpdated event = MissionPlanUpdated.create(12);

    assertEquals("MissionPlanUpdated", event.eventType());
    assertEquals(12, event.missionPlanId());
    assertEnvelope(event.envelope(), "12");
  }

  @Test
  void scheduleEventsShouldHaveEnvelopeAndStableTypeNames() {
    MissionScheduleCreated created = MissionScheduleCreated.create("schedule-1", "plan-1");
    MissionScheduleUpdated updated = MissionScheduleUpdated.create("schedule-1");
    ModuleStateChangeScheduled stateChange =
        ModuleStateChangeScheduled.create("schedule-1", 7, "habitat-1", ModuleState.DESTROYED);

    assertEquals("MissionScheduleCreated", created.eventType());
    assertEquals("MissionScheduleUpdated", updated.eventType());
    assertEquals("ModuleStateChangeScheduled", stateChange.eventType());
    assertEnvelope(created.envelope(), "schedule-1");
    assertEnvelope(updated.envelope(), "schedule-1");
    assertEnvelope(stateChange.envelope(), "schedule-1");
  }

  @Test
  void integrationEventsShouldNotDependOnSpring() {
    var classes =
        new ClassFileImporter()
            .withImportOption(new ImportOption.DoNotIncludeTests())
            .importPackages("miasi.backend.events");

    ArchRuleDefinition.noClasses()
        .that()
        .resideInAPackage("miasi.backend.events..")
        .should()
        .dependOnClassesThat()
        .resideInAPackage("org.springframework..")
        .check(classes);
  }

  private void assertEnvelope(
      miasi.backend.sharedkernel.events.EventEnvelope envelope, String aggregateId) {
    assertNotNull(envelope);
    assertNotNull(envelope.eventId());
    assertNotNull(envelope.occurredAt());
    assertEquals(1, envelope.schemaVersion());
    assertEquals(aggregateId, envelope.aggregateId());
    assertNotNull(envelope.correlationId());
    assertNull(envelope.causationId());
  }
}
