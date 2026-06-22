package miasi.backend.common.domain.model.event;

public record MissionScheduleCreated(
    EventEnvelope envelope, String scheduleId, String missionPlanId) implements IntegrationEvent {

  public static MissionScheduleCreated create(String scheduleId, String missionPlanId) {
    return new MissionScheduleCreated(EventEnvelope.initial(scheduleId), scheduleId, missionPlanId);
  }

  @Override
  public String eventType() {
    return "MissionScheduleCreated";
  }
}
