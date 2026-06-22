package miasi.backend.sharedkernel.events;

public interface IntegrationEvent {
  EventEnvelope envelope();

  String eventType();
}
