package miasi.backend.events;

import java.util.List;
import miasi.backend.schedule.domain.DeliveryItemType;
import miasi.backend.sharedkernel.events.EventEnvelope;
import miasi.backend.sharedkernel.events.IntegrationEvent;

public record SupplyDeliveryScheduled(
    EventEnvelope envelope,
    String scheduleId,
    int sol,
    List<DeliveryItemSnapshot> items,
    double totalWeight)
    implements IntegrationEvent {

  public static SupplyDeliveryScheduled create(
      String scheduleId, int sol, List<DeliveryItemSnapshot> items, double totalWeight) {
    return new SupplyDeliveryScheduled(
        EventEnvelope.initial(scheduleId),
        scheduleId,
        sol,
        items == null ? List.of() : List.copyOf(items),
        totalWeight);
  }

  @Override
  public String eventType() {
    return "SupplyDeliveryScheduled";
  }

  public record DeliveryItemSnapshot(
      String itemId, DeliveryItemType itemType, double quantity, double weight) {}
}
