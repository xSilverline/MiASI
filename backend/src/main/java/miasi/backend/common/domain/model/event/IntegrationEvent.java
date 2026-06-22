package miasi.backend.common.domain.model.event;

public interface IntegrationEvent {
  EventEnvelope envelope();

  String eventType();
}
