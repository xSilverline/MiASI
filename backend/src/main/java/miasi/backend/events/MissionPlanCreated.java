package miasi.backend.events;

import miasi.backend.sharedkernel.events.EventEnvelope;
import miasi.backend.sharedkernel.events.IntegrationEvent;

public record MissionPlanCreated(EventEnvelope envelope, int missionPlanId)
    implements IntegrationEvent {

  public static MissionPlanCreated create(int missionPlanId) {
    return new MissionPlanCreated(
        EventEnvelope.initial(String.valueOf(missionPlanId)), missionPlanId);
  }

  @Override
  public String eventType() {
    return "MissionPlanCreated";
  }
}
