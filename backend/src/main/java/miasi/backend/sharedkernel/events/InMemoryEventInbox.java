package miasi.backend.sharedkernel.events;

import java.time.Clock;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;

public class InMemoryEventInbox<T extends IntegrationEvent> {
  private final ConcurrentMap<UUID, EventInboxEntry<T>> entries = new ConcurrentHashMap<>();
  private final Clock clock;

  public InMemoryEventInbox() {
    this(Clock.systemUTC());
  }

  InMemoryEventInbox(Clock clock) {
    this.clock = clock;
  }

  public boolean record(T event) {
    return handle(event, ignored -> {});
  }

  public boolean handle(T event, Consumer<T> processor) {
    if (entries.containsKey(event.envelope().eventId())) {
      return false;
    }

    Instant now = Instant.now(clock);
    try {
      processor.accept(event);
      return entries.putIfAbsent(event.envelope().eventId(), EventInboxEntry.processed(event, now))
          == null;
    } catch (RuntimeException exception) {
      entries.putIfAbsent(
          event.envelope().eventId(), EventInboxEntry.failed(event, now, exception));
      throw exception;
    }
  }

  public List<EventInboxEntry<T>> entries() {
    return entries.values().stream()
        .sorted(Comparator.comparing(EventInboxEntry::receivedAt))
        .toList();
  }

  public List<T> events() {
    return entries().stream().map(EventInboxEntry::event).toList();
  }

  public void clear() {
    entries.clear();
  }
}
