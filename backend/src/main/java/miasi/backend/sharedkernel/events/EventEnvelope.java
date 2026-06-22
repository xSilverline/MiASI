package miasi.backend.sharedkernel.events;

import java.time.Instant;
import java.util.UUID;

public record EventEnvelope(
    UUID eventId,
    Instant occurredAt,
    int schemaVersion,
    String aggregateId,
    UUID correlationId,
    UUID causationId) {

  public static EventEnvelope initial(String aggregateId) {
    return new EventEnvelope(
        UUID.randomUUID(), Instant.now(), 1, aggregateId, UUID.randomUUID(), null);
  }
}
