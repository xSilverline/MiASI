package miasi.backend.events;

import miasi.backend.sharedkernel.events.EventEnvelope;
import miasi.backend.sharedkernel.events.IntegrationEvent;

public record MissionPlanUpdated(EventEnvelope envelope, int missionPlanId)
    implements IntegrationEvent {

  public static MissionPlanUpdated create(int missionPlanId) {
    return new MissionPlanUpdated(
        EventEnvelope.initial(String.valueOf(missionPlanId)), missionPlanId);
  }

  @Override
  public String eventType() {
    return "MissionPlanUpdated";
  }
}
