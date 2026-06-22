package miasi.backend.domains.schedule;

import miasi.backend.domains.schedule.enums.DeliveryItemType;
import miasi.backend.domains.schedule.enums.EventType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
        deliveryWithItems(
            List.of(new DeliveryItem("water", DeliveryItemType.RESOURCE, 3.0, 20.0)));
    // When + Then
    assertThrows(
        IllegalArgumentException.class, () -> policy.validateDeliveryWeight(delivery, 50.0));
  }

  @Test
  void allowManyEventsInSameSol_shouldAllowMultipleEvents() {
    // When + Then
    assertTrue(
        policy.allowManyEventsInSameSol(
            eventAtSol(12), MissionSchedule.createDraft("plan-1", 120)));
  }

  private ScheduledEvent eventAtSol(int sol) {
    return new ScheduledEvent("event-" + sol, EventType.THREAT, sol, "description");
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
