package miasi.backend.database;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.IntStream;
import lombok.AccessLevel;
import lombok.Synchronized;
import lombok.experimental.FieldDefaults;
import miasi.backend.domains.schedule.EventDefinition;
import miasi.backend.domains.schedule.application.port.out.IEventCatalogRepositoryPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

@Repository
@FieldDefaults(level = AccessLevel.PRIVATE)
public final class EventCatalogRepository implements IEventCatalogRepositoryPort {
  final ModuleRecordList<EventDefinition> events;

  public EventCatalogRepository(@Value("${database.filename.event.catalog}") String filePath)
      throws IOException {
    JsonFileStorage<EventDefinition> storage = new JsonFileStorage<>(EventDefinition.class);
    this.events = new ModuleRecordList<>(loadFile(storage, filePath), filePath, storage);
  }

  @Synchronized
  @Override
  public List<EventDefinition> findAll() {
    return List.copyOf(events.getObjects());
  }

  @Synchronized
  @Override
  public Optional<EventDefinition> findById(String eventId) {
    validateEventId(eventId);
    return events.getObjects().stream()
        .filter(event -> Objects.equals(event.getId(), eventId))
        .findFirst();
  }

  @Synchronized
  @Override
  public EventDefinition save(EventDefinition event) {
    validate(event);
    if (event.getId() == null || event.getId().isBlank()) {
      event.setId(UUID.randomUUID().toString());
    }

    List<EventDefinition> list = events.getObjects();
    int index = findIndexById(event.getId());

    if (index == -1) {
      list.add(event);
    } else {
      list.set(index, event);
    }

    events.save();
    return event;
  }

  @Synchronized
  @Override
  public List<EventDefinition> saveAll(List<EventDefinition> eventDefinitions) {
    if (eventDefinitions == null || eventDefinitions.isEmpty()) {
      return List.of();
    }
    return eventDefinitions.stream().map(this::save).toList();
  }

  @Synchronized
  @Override
  public EventDefinition update(String eventId, EventDefinition event) {
    validateEventId(eventId);
    validate(event);

    int index = findIndexById(eventId);
    if (index == -1) {
      throw new IllegalArgumentException("Event definition not found: " + eventId);
    }

    event.setId(eventId);
    events.getObjects().set(index, event);
    events.save();
    return event;
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

  private int findIndexById(String eventId) {
    return IntStream.range(0, events.getObjects().size())
        .filter(index -> Objects.equals(events.getObjects().get(index).getId(), eventId))
        .findFirst()
        .orElse(-1);
  }

  private <T> List<T> loadFile(JsonFileStorage<T> database, String fileName) throws IOException {
    List<T> loaded = database.loadListFromFile(fileName);
    return loaded != null ? loaded : new ArrayList<>();
  }

  private void validate(EventDefinition event) {
    if (event == null) {
      throw new IllegalArgumentException("Event definition is required");
    }
    if (event.getName() == null || event.getName().isBlank()) {
      throw new IllegalArgumentException("Event definition name is required");
    }
    if (event.getType() == null) {
      throw new IllegalArgumentException("Event definition type is required");
    }
  }

  private void validateEventId(String eventId) {
    if (eventId == null || eventId.isBlank()) {
      throw new IllegalArgumentException("Event definition id is required");
    }
  }
}
