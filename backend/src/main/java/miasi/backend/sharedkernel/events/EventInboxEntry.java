package miasi.backend.sharedkernel.events;

import java.time.Instant;
import java.util.UUID;

public record EventInboxEntry<T extends IntegrationEvent>(
    UUID eventId,
    String eventType,
    String aggregateId,
    EventProcessingStatus status,
    Instant receivedAt,
    Instant processedAt,
    String error,
    T event) {

  static <T extends IntegrationEvent> EventInboxEntry<T> processed(T event, Instant now) {
    return new EventInboxEntry<>(
        event.envelope().eventId(),
        event.eventType(),
        event.envelope().aggregateId(),
        EventProcessingStatus.PROCESSED,
        now,
        now,
        null,
        event);
  }

  static <T extends IntegrationEvent> EventInboxEntry<T> failed(
      T event, Instant now, RuntimeException exception) {
    return new EventInboxEntry<>(
        event.envelope().eventId(),
        event.eventType(),
        event.envelope().aggregateId(),
        EventProcessingStatus.FAILED,
        now,
        now,
        exception.getMessage(),
        event);
  }
}
