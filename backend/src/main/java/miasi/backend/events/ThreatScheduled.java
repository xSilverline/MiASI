package miasi.backend.events;

import miasi.backend.schedule.domain.ThreatType;
import miasi.backend.sharedkernel.events.EventEnvelope;
import miasi.backend.sharedkernel.events.IntegrationEvent;

public record ThreatScheduled(
    EventEnvelope envelope,
    String scheduleId,
    int sol,
    ThreatType threatType,
    String affectedElement,
    double impactValue,
    int durationSols,
    String impactUnit)
    implements IntegrationEvent {

  public static ThreatScheduled create(
      String scheduleId,
      int sol,
      ThreatType threatType,
      String affectedElement,
      double impactValue,
      int durationSols,
      String impactUnit) {
    return new ThreatScheduled(
        EventEnvelope.initial(scheduleId),
        scheduleId,
        sol,
        threatType,
        affectedElement,
        impactValue,
        durationSols,
        impactUnit);
  }

  @Override
  public String eventType() {
    return "ThreatScheduled";
  }
}
