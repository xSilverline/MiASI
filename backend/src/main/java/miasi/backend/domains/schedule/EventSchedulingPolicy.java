package miasi.backend.domains.schedule;

public class EventSchedulingPolicy {
  public void validateSolWithinMission(ScheduledEvent event, int durationSols) {
  }

  public void validateDeliveryWeight(SupplyDelivery delivery, double cargoLimit) {
  }

  public boolean allowManyEventsInSameSol(ScheduledEvent event, MissionSchedule schedule) {
    return true;
  }
}
