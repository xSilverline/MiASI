package miasi.backend.domains.schedule.application.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import miasi.backend.domains.schedule.EventDefinition;
import miasi.backend.domains.schedule.application.port.out.IEventCatalogRepositoryPort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EventCatalogApplicationService {

  private final IEventCatalogRepositoryPort repository;

  public List<EventDefinition> getCatalog() {
    return repository.findAll();
  }

  public EventDefinition add(EventDefinition event) {
    return repository.save(event);
  }

  public List<EventDefinition> addAll(List<EventDefinition> events) {
    if (events == null || events.isEmpty()) {
      return List.of();
    }
    return repository.saveAll(events);
  }

  public EventDefinition update(String eventId, EventDefinition event) {
    return repository.update(eventId, event);
  }

  public boolean delete(String eventId) {
    return repository.delete(eventId);
  }
}
