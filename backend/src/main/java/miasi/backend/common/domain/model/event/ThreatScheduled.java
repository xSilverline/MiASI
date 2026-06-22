package miasi.backend.common.domain.model.event;

import miasi.backend.schedule.domain.model.ThreatType;

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
