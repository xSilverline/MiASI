package miasi.backend.domains.schedule.infrastructure.in.web;

import io.swagger.v3.oas.annotations.Operation;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import miasi.backend.api.jsons.BasicResponseEntity;
import miasi.backend.domains.schedule.ScheduledEvent;
import miasi.backend.domains.schedule.application.service.TimelineApplicationService;
import miasi.backend.domains.schedule.infrastructure.in.web.dto.TimelineEventRequest;
import miasi.backend.domains.schedule.infrastructure.in.web.dto.TimelineSolResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "http://localhost:*")
@RestController
@RequestMapping("/api/timeline")
@RequiredArgsConstructor
public class TimelineController {

  private final TimelineApplicationService service;

  @GetMapping
  @Operation(summary = "Pobiera globalną oś czasu pogrupowaną po solach")
  public ResponseEntity<List<TimelineSolResponse>> getTimeline() {
    return ResponseEntity.ok(TimelineSolResponse.fromTimelineEvents(service.getTimelineEvents()));
  }

  @GetMapping("/deliveries")
  @Operation(summary = "Pobiera dostawy z osi czasu")
  public ResponseEntity<List<TimelineSolResponse>> getDeliveries() {
    return ResponseEntity.ok(TimelineSolResponse.fromEvents(service.getDeliveries()));
  }

  @GetMapping("/threats")
  @Operation(summary = "Pobiera zagrożenia z osi czasu")
  public ResponseEntity<List<TimelineSolResponse>> getThreats() {
    return ResponseEntity.ok(TimelineSolResponse.fromEvents(service.getThreats()));
  }

  @PostMapping("/events")
  @Operation(summary = "Dodaje event z katalogu do konkretnego sola")
  public ResponseEntity<ScheduledEvent> addEvent(@RequestBody TimelineEventRequest request) {
    ScheduledEvent saved = service.addCatalogEvent(request.sol(), request.eventDefinitionId());
    return ResponseEntity.created(URI.create("/api/timeline/events/%s".formatted(saved.getId())))
        .body(saved);
  }

  @PostMapping("/events/batch")
  @Operation(summary = "Dodaje listę eventów z katalogu do konkretnych soli")
  public ResponseEntity<List<ScheduledEvent>> addEvents(
      @RequestBody List<TimelineEventRequest> requests) {
    List<TimelineApplicationService.TimelineEventCommand> commands =
        requests == null
            ? List.of()
            : requests.stream()
                .map(
                    request ->
                        new TimelineApplicationService.TimelineEventCommand(
                            request.sol(), request.eventDefinitionId()))
                .toList();
    return ResponseEntity.ok(service.addCatalogEvents(commands));
  }

  @DeleteMapping("/events/{eventId}")
  @Operation(summary = "Usuwa event z osi czasu po id")
  public ResponseEntity<BasicResponseEntity> deleteEvent(@PathVariable String eventId) {
    return service.deleteEvent(eventId)
        ? ResponseEntity.ok(BasicResponseEntity.success("Timeline event removed"))
        : ResponseEntity.notFound().build();
  }

  @DeleteMapping("/sols/{sol}/events/{eventId}")
  @Operation(summary = "Usuwa event z konkretnego sola")
  public ResponseEntity<BasicResponseEntity> deleteEventFromSol(
      @PathVariable int sol, @PathVariable String eventId) {
    return service.deleteEventFromSol(sol, eventId)
        ? ResponseEntity.ok(BasicResponseEntity.success("Timeline event removed"))
        : ResponseEntity.notFound().build();
  }
}
