package miasi.backend.common.domain.model.event;

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
