package miasi.backend.domains.schedule.application.port.out;

import java.util.List;
import miasi.backend.domains.schedule.ScheduledEvent;
import miasi.backend.domains.schedule.enums.EventType;

public interface ITimelineRepositoryPort {

  List<ScheduledEvent> findAll();

  List<ScheduledEvent> findByType(EventType type);

  ScheduledEvent save(ScheduledEvent event);

  List<ScheduledEvent> saveAll(List<ScheduledEvent> events);

  boolean delete(String eventId);

  boolean deleteFromSol(int sol, String eventId);
}
