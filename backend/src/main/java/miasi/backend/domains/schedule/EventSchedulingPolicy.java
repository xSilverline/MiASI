package miasi.backend.domains.schedule;

public class EventSchedulingPolicy {
  public void validateSolWithinMission(ScheduledEvent event, int durationSols) {
    if (event == null) {
      throw new IllegalArgumentException("Scheduled event is required");
    }
    if (durationSols < 1) {
      throw new IllegalArgumentException("Mission duration must be at least 1 sol");
    }
    if (event.getSol() < 1 || event.getSol() > durationSols) {
      throw new IllegalArgumentException(
          "Event sol must be between 1 and mission duration: " + durationSols);
    }
  }

  public void validateDeliveryWeight(SupplyDelivery delivery, double cargoLimit) {
    if (delivery == null) {
      throw new IllegalArgumentException("Supply delivery is required");
    }
    if (cargoLimit < 0) {
      throw new IllegalArgumentException("Cargo limit cannot be negative");
    }

    DeliveryContent content = delivery.getContent();
    double totalWeight = 0.0;
    if (content != null && content.getItems() != null) {
      totalWeight =
          content.getItems().stream()
              .mapToDouble(item -> item.getQuantity() * item.getWeight())
              .sum();
    }

    if (totalWeight > cargoLimit) {
      throw new IllegalArgumentException(
          "Delivery weight " + totalWeight + " exceeds cargo limit " + cargoLimit);
    }
  }

  public boolean allowManyEventsInSameSol(ScheduledEvent event, MissionSchedule schedule) {
    return true;
  }
}
