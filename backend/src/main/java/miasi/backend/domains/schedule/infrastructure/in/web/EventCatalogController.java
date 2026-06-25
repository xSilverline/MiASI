package miasi.backend.domains.schedule.infrastructure.in.web;

import io.swagger.v3.oas.annotations.Operation;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import miasi.backend.api.jsons.BasicResponseEntity;
import miasi.backend.domains.schedule.EventDefinition;
import miasi.backend.domains.schedule.application.service.EventCatalogApplicationService;
import miasi.backend.domains.schedule.enums.EventType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/event-catalog")
@RequiredArgsConstructor
public class EventCatalogController {

  private final EventCatalogApplicationService service;

  @GetMapping
  @Operation(summary = "Pobiera globalny katalog definicji eventów")
  public ResponseEntity<List<EventDefinition>> getCatalog() {
    return ResponseEntity.ok(service.getCatalog());
  }

  @GetMapping("/types")
  @Operation(summary = "Pobiera typy eventów używane w katalogu i timeline")
  public ResponseEntity<EventType[]> getEventTypes() {
    return ResponseEntity.ok(EventType.values());
  }

  @PostMapping
  @Operation(summary = "Dodaje definicję eventu do globalnego katalogu")
  public ResponseEntity<EventDefinition> add(@RequestBody EventDefinition event) {
    EventDefinition saved = service.add(event);
    return ResponseEntity.created(URI.create("/api/event-catalog/%s".formatted(saved.getId())))
        .body(saved);
  }

  @PostMapping("/batch")
  @Operation(summary = "Dodaje listę definicji eventów do globalnego katalogu")
  public ResponseEntity<List<EventDefinition>> addAll(@RequestBody List<EventDefinition> events) {
    return ResponseEntity.ok(service.addAll(events));
  }

  @PutMapping("/{eventId}")
  @Operation(summary = "Aktualizuje definicję eventu w globalnym katalogu")
  public ResponseEntity<EventDefinition> update(
      @PathVariable String eventId, @RequestBody EventDefinition event) {
    return ResponseEntity.ok(service.update(eventId, event));
  }

  @DeleteMapping("/{eventId}")
  @Operation(summary = "Usuwa definicję eventu z globalnego katalogu")
  public ResponseEntity<BasicResponseEntity> delete(@PathVariable String eventId) {
    return service.delete(eventId)
        ? ResponseEntity.ok(BasicResponseEntity.success("Event definition removed"))
        : ResponseEntity.notFound().build();
  }
}
