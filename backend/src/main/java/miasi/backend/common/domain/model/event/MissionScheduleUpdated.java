package miasi.backend.common.domain.model.event;

public record MissionScheduleUpdated(EventEnvelope envelope, String scheduleId)
    implements IntegrationEvent {

  public static MissionScheduleUpdated create(String scheduleId) {
    return new MissionScheduleUpdated(EventEnvelope.initial(scheduleId), scheduleId);
  }

  @Override
  public String eventType() {
    return "MissionScheduleUpdated";
  }
}
