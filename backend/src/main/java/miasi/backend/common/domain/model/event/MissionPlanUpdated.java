package miasi.backend.common.domain.model.event;

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
