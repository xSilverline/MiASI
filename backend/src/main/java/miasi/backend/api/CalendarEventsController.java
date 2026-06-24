package miasi.backend.api;

import io.swagger.v3.oas.annotations.Operation;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import miasi.backend.api.jsons.EventTimelineDayResponse;
import miasi.backend.domains.schedule.ScheduleService;
import miasi.backend.domains.schedule.ScheduledEvent;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "http://localhost:*")
@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class CalendarEventsController {

  private final ScheduleService scheduleService;

  @GetMapping
  @Operation(
      summary = "Pobiera eventy kalendarza",
      description =
          "Zwraca eventy z planu misji albo draftu scenariusza. context=schedule używa scheduleId,"
              + " a context=scenario używa draftId jako contextId.")
  public ResponseEntity<List<ScheduledEvent>> getEvents(
      @RequestParam String context, @RequestParam String contextId) {
    EventContext parsedContext = EventContext.from(context);
    return ResponseEntity.ok(getEventsFor(parsedContext, contextId));
  }

  @GetMapping("/timeline")
  @Operation(
      summary = "Pobiera eventy dzień po dniu",
      description =
          "Zwraca timeline z wpisem dla każdego solu. Każdy dzień zawiera listę eventów z planu"
              + " misji albo draftu scenariusza.")
  public ResponseEntity<List<EventTimelineDayResponse>> getTimeline(
      @RequestParam String context, @RequestParam String contextId) {
    EventContext parsedContext = EventContext.from(context);
    return ResponseEntity.ok(getTimelineFor(parsedContext, contextId));
  }

  @PutMapping("/timeline")
  @Operation(
      summary = "Zastępuje timeline eventów",
      description =
          "Podmienia wszystkie eventy w planie misji albo drafcie scenariusza na eventy z osi"
              + " czasu dzień po dniu.")
  public ResponseEntity<List<EventTimelineDayResponse>> replaceTimeline(
      @RequestParam String context,
      @RequestParam String contextId,
      @RequestBody List<EventTimelineDayResponse> timeline) {
    EventContext parsedContext = EventContext.from(context);
    replaceEventsFor(parsedContext, contextId, flattenTimeline(timeline));
    return ResponseEntity.ok(getTimelineFor(parsedContext, contextId));
  }

  @PutMapping("/timeline/{sol}")
  @Operation(
      summary = "Zastępuje eventy dla wybranego solu",
      description =
          "Podmienia listę eventów tylko dla jednego dnia w planie misji albo drafcie"
              + " scenariusza.")
  public ResponseEntity<List<EventTimelineDayResponse>> replaceTimelineDay(
      @PathVariable int sol,
      @RequestParam String context,
      @RequestParam String contextId,
      @RequestBody EventTimelineDayResponse day) {
    return replaceTimelineDayEvents(sol, context, contextId, day);
  }

  @PatchMapping("/timeline/{sol}")
  @Operation(
      summary = "Aktualizuje eventy dla wybranego solu",
      description =
          "Aktualizuje listę eventów tylko dla jednego dnia w planie misji albo drafcie"
              + " scenariusza.")
  public ResponseEntity<List<EventTimelineDayResponse>> patchTimelineDay(
      @PathVariable int sol,
      @RequestParam String context,
      @RequestParam String contextId,
      @RequestBody EventTimelineDayResponse day) {
    return replaceTimelineDayEvents(sol, context, contextId, day);
  }

  private ResponseEntity<List<EventTimelineDayResponse>> replaceTimelineDayEvents(
      int sol, String context, String contextId, EventTimelineDayResponse day) {
    EventContext parsedContext = EventContext.from(context);
    replaceEventsAtSolFor(parsedContext, contextId, sol, eventsForSol(sol, day));
    return ResponseEntity.ok(getTimelineFor(parsedContext, contextId));
  }

  private List<EventTimelineDayResponse> getTimelineFor(EventContext context, String contextId) {
    return EventTimelineDayResponse.from(
        getEventsFor(context, contextId), getDurationFor(context, contextId));
  }

  @PostMapping
  @Operation(
      summary = "Dodaje event do kalendarza",
      description =
          "Dodaje event do planu misji albo draftu scenariusza i zwraca aktualną listę eventów.")
  public ResponseEntity<List<ScheduledEvent>> addEvent(
      @RequestParam String context,
      @RequestParam String contextId,
      @RequestBody ScheduledEvent event) {
    EventContext parsedContext = EventContext.from(context);
    switch (parsedContext) {
      case SCHEDULE -> scheduleService.addEvent(contextId, event);
      case SCENARIO -> scheduleService.addScenarioEvent(contextId, event);
    }
    return ResponseEntity.created(
            URI.create(
                "/api/events?context=%s&contextId=%s"
                    .formatted(parsedContext.name().toLowerCase(), contextId)))
        .body(getEventsFor(parsedContext, contextId));
  }

  @PutMapping("/{eventId}")
  @Operation(
      summary = "Edytuje event kalendarza",
      description =
          "Aktualizuje event w planie misji albo draftu scenariusza i zwraca aktualną listę eventów.")
  public ResponseEntity<List<ScheduledEvent>> updateEvent(
      @PathVariable String eventId,
      @RequestParam String context,
      @RequestParam String contextId,
      @RequestBody ScheduledEvent event) {
    EventContext parsedContext = EventContext.from(context);
    switch (parsedContext) {
      case SCHEDULE -> scheduleService.updateEvent(contextId, eventId, event);
      case SCENARIO -> scheduleService.correctScenarioEvent(contextId, eventId, event);
    }
    return ResponseEntity.ok(getEventsFor(parsedContext, contextId));
  }

  @DeleteMapping("/{eventId}")
  @Operation(
      summary = "Usuwa event z kalendarza",
      description =
          "Usuwa event z planu misji albo draftu scenariusza i zwraca aktualną listę eventów.")
  public ResponseEntity<List<ScheduledEvent>> deleteEvent(
      @PathVariable String eventId,
      @RequestParam String context,
      @RequestParam String contextId) {
    EventContext parsedContext = EventContext.from(context);
    switch (parsedContext) {
      case SCHEDULE -> scheduleService.removeEvent(contextId, eventId);
      case SCENARIO -> scheduleService.removeScenarioEvent(contextId, eventId);
    }
    return ResponseEntity.ok(getEventsFor(parsedContext, contextId));
  }

  private List<ScheduledEvent> getEventsFor(EventContext context, String contextId) {
    return switch (context) {
      case SCHEDULE -> scheduleService.getScheduleEvents(contextId);
      case SCENARIO -> scheduleService.getScenarioEvents(contextId);
    };
  }

  private void replaceEventsFor(
      EventContext context, String contextId, List<ScheduledEvent> events) {
    switch (context) {
      case SCHEDULE -> scheduleService.replaceEvents(contextId, events);
      case SCENARIO -> scheduleService.replaceScenarioEvents(contextId, events);
    }
  }

  private void replaceEventsAtSolFor(
      EventContext context, String contextId, int sol, List<ScheduledEvent> events) {
    switch (context) {
      case SCHEDULE -> scheduleService.replaceEventsAtSol(contextId, sol, events);
      case SCENARIO -> scheduleService.replaceScenarioEventsAtSol(contextId, sol, events);
    }
  }

  private List<ScheduledEvent> flattenTimeline(List<EventTimelineDayResponse> timeline) {
    if (timeline == null) {
      return List.of();
    }
    return timeline.stream().flatMap(day -> eventsForDay(day).stream()).toList();
  }

  private List<ScheduledEvent> eventsForDay(EventTimelineDayResponse day) {
    if (day == null || day.events() == null) {
      return List.of();
    }
    if (day.sol() < 1) {
      throw new IllegalArgumentException("Timeline day sol must be greater than 0");
    }
    return eventsForSol(day.sol(), day);
  }

  private List<ScheduledEvent> eventsForSol(int sol, EventTimelineDayResponse day) {
    if (sol < 1) {
      throw new IllegalArgumentException("Timeline day sol must be greater than 0");
    }
    if (day == null || day.events() == null) {
      return List.of();
    }
    if (day.sol() > 0 && day.sol() != sol) {
      throw new IllegalArgumentException("Timeline day sol must match path sol");
    }
    return day.events().stream()
        .filter(event -> event != null)
        .peek(event -> event.setSol(sol))
        .toList();
  }

  private int getDurationFor(EventContext context, String contextId) {
    return switch (context) {
      case SCHEDULE -> scheduleService.getSchedule(contextId).getDurationSols();
      case SCENARIO -> scheduleService.getScenarioDraft(contextId).getDurationSols();
    };
  }

  public enum EventContext {
    SCHEDULE,
    SCENARIO;

    public static EventContext from(String value) {
      if (value == null || value.isBlank()) {
        throw new IllegalArgumentException("Event context is required");
      }
      return EventContext.valueOf(value.trim().toUpperCase());
    }
  }
}
