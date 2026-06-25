package miasi.backend.domains.schedule.application.service;

import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import miasi.backend.domains.schedule.EventDefinition;
import miasi.backend.domains.schedule.ScheduledEvent;
import miasi.backend.domains.schedule.application.port.out.IEventCatalogRepositoryPort;
import miasi.backend.domains.schedule.application.port.out.ITimelineRepositoryPort;
import miasi.backend.domains.schedule.enums.EventType;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TimelineApplicationService {

  private final ITimelineRepositoryPort timelineRepository;
  private final IEventCatalogRepositoryPort eventCatalogRepository;

  public List<ScheduledEvent> getTimelineEvents() {
    return sorted(timelineRepository.findAll());
  }

  public List<ScheduledEvent> getDeliveries() {
    return sorted(timelineRepository.findByType(EventType.SUPPLY_DELIVERY));
  }

  public List<ScheduledEvent> getThreats() {
    return sorted(timelineRepository.findByType(EventType.THREAT));
  }

  public ScheduledEvent addCatalogEvent(int sol, String eventDefinitionId) {
    EventDefinition definition =
        eventCatalogRepository
            .findById(eventDefinitionId)
            .orElseThrow(
                () ->
                    new NoSuchElementException("Event definition not found: " + eventDefinitionId));

    ScheduledEvent event =
        new ScheduledEvent(
            UUID.randomUUID().toString(),
            definition.getType(),
            sol,
            definition.getDescription(),
            definition.getEffects());
    return timelineRepository.save(event);
  }

  public List<ScheduledEvent> addCatalogEvents(List<TimelineEventCommand> commands) {
    if (commands == null || commands.isEmpty()) {
      return List.of();
    }

    List<ScheduledEvent> events =
        commands.stream()
            .map(command -> addCatalogEvent(command.sol(), command.eventDefinitionId()))
            .toList();
    return sorted(events);
  }

  public boolean deleteEvent(String eventId) {
    return timelineRepository.delete(eventId);
  }

  public boolean deleteEventFromSol(int sol, String eventId) {
    return timelineRepository.deleteFromSol(sol, eventId);
  }

  private List<ScheduledEvent> sorted(List<ScheduledEvent> events) {
    return events.stream()
        .sorted(
            Comparator.comparingInt(ScheduledEvent::getSol).thenComparing(ScheduledEvent::getId))
        .toList();
  }

  public record TimelineEventCommand(int sol, String eventDefinitionId) {}
}
