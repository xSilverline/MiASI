package miasi.backend.api;

import io.swagger.v3.oas.annotations.Operation;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import miasi.backend.api.config.ConfService;
import miasi.backend.api.jsons.BasicResponseEntity;
import miasi.backend.domains.schedule.EventDefinition;
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

@CrossOrigin(origins = "http://localhost:*")
@RestController
@RequestMapping("/api/event-definitions")
@RequiredArgsConstructor
public class EventDefinitionsController {

  private final ConfService confService;

  @GetMapping
  @Operation(
      summary = "Pobiera zapisane definicje eventów",
      description =
          "Zwraca eventy zapisane w pamięci jako presety użytkownika. To nie są jeszcze eventy"
              + " umieszczone w kalendarzu.")
  public ResponseEntity<List<EventDefinition>> getEventDefinitions() {
    return ResponseEntity.ok(confService.getEventCatalog());
  }

  @PostMapping
  @Operation(
      summary = "Tworzy definicję eventu",
      description = "Zapisuje definicję eventu/preset do późniejszego użycia w kalendarzu.")
  public ResponseEntity<EventDefinition> createEventDefinition(@RequestBody EventDefinition event) {
    EventDefinition saved = confService.addEventDefinition(event);
    return ResponseEntity.created(URI.create("/api/event-definitions/%s".formatted(saved.getId())))
        .body(saved);
  }

  @PutMapping("/{eventDefinitionId}")
  @Operation(
      summary = "Edytuje definicję eventu",
      description =
          "Aktualizuje zapisany preset. Nie zmienia eventów już dodanych do schedule albo scenario.")
  public ResponseEntity<EventDefinition> updateEventDefinition(
      @PathVariable String eventDefinitionId, @RequestBody EventDefinition event) {
    return ResponseEntity.ok(confService.updateEventDefinition(eventDefinitionId, event));
  }

  @DeleteMapping("/{eventDefinitionId}")
  @Operation(
      summary = "Usuwa definicję eventu",
      description =
          "Usuwa zapisany preset. Nie usuwa eventów już dodanych do schedule albo scenario.")
  public ResponseEntity<BasicResponseEntity> deleteEventDefinition(
      @PathVariable String eventDefinitionId) {
    return confService.deleteEventDefinition(eventDefinitionId)
        ? ResponseEntity.ok(BasicResponseEntity.success("Event definition removed"))
        : ResponseEntity.notFound().build();
  }
}
