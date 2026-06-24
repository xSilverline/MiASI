package miasi.backend.domains.schedule;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import miasi.backend.domains.schedule.enums.DeliveryItemType;
import miasi.backend.domains.schedule.enums.EventType;
import org.junit.jupiter.api.Test;

class EventSchedulingPolicyTest {

  private final EventSchedulingPolicy policy = new EventSchedulingPolicy();

  @Test
  void validateSolWithinMission_shouldAllowBoundarySols() {
    // When + Then
    assertDoesNotThrow(() -> policy.validateSolWithinMission(eventAtSol(1), 30));
    assertDoesNotThrow(() -> policy.validateSolWithinMission(eventAtSol(30), 30));
  }

  @Test
  void validateSolWithinMission_shouldThrowWhenSolIsBeforeMission() {
    // When + Then
    assertThrows(
        IllegalArgumentException.class, () -> policy.validateSolWithinMission(eventAtSol(0), 30));
  }

  @Test
  void validateSolWithinMission_shouldThrowWhenSolIsAfterMission() {
    // When + Then
    assertThrows(
        IllegalArgumentException.class, () -> policy.validateSolWithinMission(eventAtSol(31), 30));
  }

  @Test
  void validateDeliveryWeight_shouldAllowWeightEqualToLimit() {
    // Given
    SupplyDelivery delivery =
        deliveryWithItems(
            List.of(
                new DeliveryItem("water", DeliveryItemType.RESOURCE, 2.0, 10.0),
                new DeliveryItem("module", DeliveryItemType.MODULE, 1.0, 30.0)));
    // When + Then
    assertDoesNotThrow(() -> policy.validateDeliveryWeight(delivery, 50.0));
  }

  @Test
  void validateDeliveryWeight_shouldThrowWhenWeightExceedsLimit() {
    // Given
    SupplyDelivery delivery =
        deliveryWithItems(List.of(new DeliveryItem("water", DeliveryItemType.RESOURCE, 3.0, 20.0)));
    // When + Then
    assertThrows(
        IllegalArgumentException.class, () -> policy.validateDeliveryWeight(delivery, 50.0));
  }

  @Test
  void allowManyEventsInSameSol_shouldAllowMultipleEvents() {
    // Given
    MissionSchedule schedule = MissionSchedule.createDraft("plan-1", 120);

    // When
    schedule.addEvent(event("event-1", 12));

    // Then
    assertTrue(policy.allowManyEventsInSameSol(event("event-2", 12), schedule));
  }

  @Test
  void allowManyEventsInSameSol_shouldRejectMultipleEventsWhenPolicyDisallowsThem() {
    // Given
    EventSchedulingPolicy strictPolicy = new EventSchedulingPolicy(false);
    MissionSchedule schedule = MissionSchedule.createDraft("plan-1", 120);

    // When
    schedule.addEvent(event("event-1", 12));

    // Then
    assertFalse(strictPolicy.allowManyEventsInSameSol(event("event-2", 12), schedule));
  }

  private ScheduledEvent eventAtSol(int sol) {
    return event("event-" + sol, sol);
  }

  private ScheduledEvent event(String id, int sol) {
    return new ScheduledEvent(id, EventType.THREAT, sol, "description");
  }

  private SupplyDelivery deliveryWithItems(List<DeliveryItem> items) {
    SupplyDelivery delivery = new SupplyDelivery(new DeliveryContent(items, 0.0));
    delivery.setId("delivery-1");
    delivery.setType(EventType.SUPPLY_DELIVERY);
    delivery.setSol(5);
    delivery.setDescription("description");
    return delivery;
  }
}
