package miasi.backend.domains.schedule;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import miasi.backend.enums.DeliveryItemType;
import miasi.backend.enums.EventType;
import org.junit.jupiter.api.Test;

class EventSchedulingPolicyTest {

  private final EventSchedulingPolicy policy = new EventSchedulingPolicy();

  @Test
  void validateSolWithinMission_shouldAllowBoundarySols() {
    assertDoesNotThrow(() -> policy.validateSolWithinMission(eventAtSol(1), 30));
    assertDoesNotThrow(() -> policy.validateSolWithinMission(eventAtSol(30), 30));
  }

  @Test
  void validateSolWithinMission_shouldThrowWhenSolIsBeforeMission() {
    assertThrows(
        IllegalArgumentException.class, () -> policy.validateSolWithinMission(eventAtSol(0), 30));
  }

  @Test
  void validateSolWithinMission_shouldThrowWhenSolIsAfterMission() {
    assertThrows(
        IllegalArgumentException.class, () -> policy.validateSolWithinMission(eventAtSol(31), 30));
  }

  @Test
  void validateDeliveryWeight_shouldAllowWeightEqualToLimit() {
    SupplyDelivery delivery =
        deliveryWithItems(
            List.of(
                new DeliveryItem("water", DeliveryItemType.RESOURCE, 2.0, 10.0),
                new DeliveryItem("module", DeliveryItemType.MODULE, 1.0, 30.0)));

    assertDoesNotThrow(() -> policy.validateDeliveryWeight(delivery, 50.0));
  }

  @Test
  void validateDeliveryWeight_shouldThrowWhenWeightExceedsLimit() {
    SupplyDelivery delivery =
        deliveryWithItems(
            List.of(new DeliveryItem("water", DeliveryItemType.RESOURCE, 3.0, 20.0)));

    assertThrows(
        IllegalArgumentException.class, () -> policy.validateDeliveryWeight(delivery, 50.0));
  }

  @Test
  void allowManyEventsInSameSol_shouldAllowMultipleEvents() {
    MissionSchedule schedule = MissionSchedule.createDraft("plan-1", 120);
    schedule.addEvent(event("event-1", 12));

    assertTrue(policy.allowManyEventsInSameSol(event("event-2", 12), schedule));
  }

  @Test
  void allowManyEventsInSameSol_shouldRejectMultipleEventsWhenPolicyDisallowsThem() {
    EventSchedulingPolicy strictPolicy = new EventSchedulingPolicy(false);
    MissionSchedule schedule = MissionSchedule.createDraft("plan-1", 120);
    schedule.addEvent(event("event-1", 12));

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
