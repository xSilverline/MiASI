package miasi.backend.domains.schedule.application.port.out;

import java.util.List;
import java.util.Optional;
import miasi.backend.domains.schedule.EventDefinition;

public interface IEventCatalogRepositoryPort {

  List<EventDefinition> findAll();

  Optional<EventDefinition> findById(String eventId);

  EventDefinition save(EventDefinition event);

  List<EventDefinition> saveAll(List<EventDefinition> events);

  EventDefinition update(String eventId, EventDefinition event);

  boolean delete(String eventId);
}
