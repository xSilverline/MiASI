package miasi.backend.domains.schedule.infrastructure.out.persistence;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Synchronized;
import lombok.experimental.FieldDefaults;
import miasi.backend.database.JsonFileStorage;
import miasi.backend.database.ModuleRecordList;
import miasi.backend.domains.schedule.ScheduledEvent;
import miasi.backend.domains.schedule.application.port.out.ITimelineRepositoryPort;
import miasi.backend.domains.schedule.enums.EventType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

@Repository
@FieldDefaults(level = AccessLevel.PRIVATE)
public final class TimelineJsonAdapter implements ITimelineRepositoryPort {

  final ModuleRecordList<ScheduledEvent> events;

  public TimelineJsonAdapter(@Value("${database.filename.timeline}") String filePath) {
    JsonFileStorage<ScheduledEvent> storage = new JsonFileStorage<>(ScheduledEvent.class);
    this.events = new ModuleRecordList<>(loadFile(storage, filePath), filePath, storage);
  }

  @Synchronized
  @Override
  public List<ScheduledEvent> findAll() {
    return List.copyOf(events.getObjects());
  }

  @Synchronized
  @Override
  public List<ScheduledEvent> findByType(EventType type) {
    return events.getObjects().stream().filter(event -> event.getType() == type).toList();
  }

  @Synchronized
  @Override
  public ScheduledEvent save(ScheduledEvent event) {
    validate(event);
    if (event.getId() == null || event.getId().isBlank()) {
      event.setId(UUID.randomUUID().toString());
    }

    events.getObjects().removeIf(existing -> Objects.equals(existing.getId(), event.getId()));
    events.getObjects().add(event);
    events.save();
    return event;
  }

  @Synchronized
  @Override
  public List<ScheduledEvent> saveAll(List<ScheduledEvent> scheduledEvents) {
    if (scheduledEvents == null || scheduledEvents.isEmpty()) {
      return List.of();
    }
    return scheduledEvents.stream().map(this::save).toList();
  }

  @Synchronized
  @Override
  public boolean delete(String eventId) {
    validateEventId(eventId);
    boolean removed = events.getObjects().removeIf(event -> Objects.equals(event.getId(), eventId));
    if (removed) {
      events.save();
    }
    return removed;
  }

  @Synchronized
  @Override
  public boolean deleteFromSol(int sol, String eventId) {
    validateEventId(eventId);
    boolean removed =
        events
            .getObjects()
            .removeIf(event -> event.getSol() == sol && Objects.equals(event.getId(), eventId));
    if (removed) {
      events.save();
    }
    return removed;
  }

  private <T> List<T> loadFile(JsonFileStorage<T> database, String fileName) {
    List<T> loaded = database.loadListFromFile(fileName);
    return loaded != null ? loaded : new ArrayList<>();
  }

  private void validate(ScheduledEvent event) {
    if (event == null) {
      throw new IllegalArgumentException("Scheduled event is required");
    }
    if (event.getType() == null) {
      throw new IllegalArgumentException("Scheduled event type is required");
    }
    if (event.getSol() < 1) {
      throw new IllegalArgumentException("Scheduled event sol must be positive");
    }
  }

  private void validateEventId(String eventId) {
    if (eventId == null || eventId.isBlank()) {
      throw new IllegalArgumentException("Scheduled event id is required");
    }
  }
}
