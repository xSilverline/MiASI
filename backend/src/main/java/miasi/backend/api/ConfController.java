package miasi.backend.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import miasi.backend.api.config.ConfService;
import miasi.backend.api.jsons.BasicResponseEntity;
import miasi.backend.domains.configuration.enums.ModuleState;
import miasi.backend.domains.configuration.enums.ResourceType;
import miasi.backend.domains.configuration.missionPlan.MissionPlan;
import miasi.backend.domains.configuration.modules.Module;
import miasi.backend.domains.configuration.modules.ModuleCategory;
import miasi.backend.domains.schedule.EventDefinition;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@CrossOrigin(origins = "http://localhost:*") // TODO: do zmiany gdy będą znane porty frontendu
@RestController
@RequestMapping("/api/conf")
@RequiredArgsConstructor
public class ConfController {

  private final ConfService confService;

  @GetMapping("/default/plan")
  public ResponseEntity<MissionPlan> getDefaultMissionPlan() {
    return ResponseEntity.ok(confService.getDefaultMissionPlan());
  }

  @GetMapping("/{missionId}/plan")
  @ApiResponses({
      @ApiResponse(
          responseCode = "200",
          description = "Plan misji został znaleziony"
      ),
      @ApiResponse(
          responseCode = "404",
          description = "Nie znaleziono planu misji o podanym id",
          content = @Content
      )
  })
  @Operation(
      summary = "Pobiera plan misji o podanym id",
      description = "Plany misji mają id w przedziale [0;X), gdzie X to wynik zapytania /api/conf/plans-count"
  )
  public ResponseEntity<MissionPlan> getMissionPlan(@PathVariable int missionId) {
    MissionPlan plan = confService.getMissionPlan(missionId);
    return plan != null ? ResponseEntity.ok(plan) : ResponseEntity.notFound().build();
  }

  @GetMapping("/module-catalog")
  public ResponseEntity<List<Module>> getModuleCatalog() {
    return ResponseEntity.ok(confService.getModuleCatalog());
  }

  @GetMapping("/event-catalog")
  @Operation(
      summary = "Pobiera katalog predefiniowanych eventów",
      description = "Zwraca słownik eventów zdefiniowanych przez użytkownika. Te definicje nie są jeszcze wpisami w harmonogramie; frontend może wybrać definicję i dopiero potem dodać ją do schedule albo scenario z konkretnym solem."
  )
  public ResponseEntity<List<EventDefinition>> getEventCatalog() {
    return ResponseEntity.ok(confService.getEventCatalog());
  }

  @PostMapping("/event-catalog")
  @Operation(
      summary = "Dodaje predefinicję eventu",
      description = "Dodaje event do katalogu konfiguracyjnego. Jeżeli id nie zostanie podane, backend wygeneruje je automatycznie."
  )
  public ResponseEntity<EventDefinition> postEventDefinition(
      @RequestBody EventDefinition event
  ) {
    EventDefinition saved = confService.addEventDefinition(event);
    return ResponseEntity
        .created(URI.create("/api/conf/event-catalog/%s".formatted(saved.getId())))
        .body(saved);
  }

  @PutMapping("/event-catalog/{eventId}")
  @Operation(
      summary = "Aktualizuje predefinicję eventu",
      description = "Aktualizuje zapisaną w katalogu definicję eventu. Operacja nie zmienia eventów już dodanych do harmonogramów ani draftów scenariuszy."
  )
  public ResponseEntity<EventDefinition> updateEventDefinition(
      @PathVariable String eventId,
      @RequestBody EventDefinition event
  ) {
    return ResponseEntity.ok(confService.updateEventDefinition(eventId, event));
  }

  @DeleteMapping("/event-catalog/{eventId}")
  @Operation(
      summary = "Usuwa predefinicję eventu",
      description = "Usuwa event z katalogu konfiguracyjnego. Operacja nie usuwa eventów już wpisanych do harmonogramów ani draftów scenariuszy."
  )
  public ResponseEntity<BasicResponseEntity> deleteEventDefinition(@PathVariable String eventId) {
    return confService.deleteEventDefinition(eventId)
        ? ResponseEntity.ok(BasicResponseEntity.success("Event definition removed"))
        : ResponseEntity.notFound().build();
  }

  @GetMapping("/event-types")
  @Operation(
      summary = "Pobiera typy eventów",
      description = "Zwraca stałą listę typów eventów używaną przy tworzeniu predefinicji i wpisów harmonogramu."
  )
  public ResponseEntity<EventType[]> getEventTypes() {
    return ResponseEntity.ok(EventType.values());
  }

  @Operation(
      summary = "Wysyła do bazy danych nowy plan misji",
      description = "Jeżeli parametr 'override' jest ustawiony, to plan nadpisze istniejacy plan na podanym id." +
          " Jeżeli podano błedne id, zwrócony zostaje komunikat NOT FOUND." +
          " Zwraca id utworzonego/nadpisanego planu jako 'message'"
  )
  @ApiResponses({
      @ApiResponse(
          responseCode = "201",
          description = "Plan został utworzony"
      ),
      @ApiResponse(
          responseCode = "404",
          description = "Nie znaleziono planu do nadpisania",
          content = @Content
      )
  })
  @PostMapping("/plan")
  public ResponseEntity<BasicResponseEntity> postMissionPlan(
      @RequestBody MissionPlan missionPlan,
      @RequestParam(required = false) Integer override
  ) {
    Integer id;
    if (override != null) {
      id = confService.overrideMissionPlan(override, missionPlan);
      if (id == null) {
        return ResponseEntity.notFound().build();
      }
    } else {
      id = confService.saveMissionPlan(missionPlan);
    }

    return ResponseEntity
        .created(URI.create("/api/conf/%d/plan".formatted(id)))
        .body(BasicResponseEntity.success(Integer.toString(id)));
  }


  @GetMapping("plans-count")
  @Operation(
      description = "Zwraca ilość planów misji w bazie danych w polu 'message'"
  )
  public ResponseEntity<BasicResponseEntity> getMissionsCount() {
    return ResponseEntity.ok()
        .body(BasicResponseEntity.success(Integer.toString(confService.getPlansCount())));
  }

  @PostMapping("/module")
  @ApiResponses({
      @ApiResponse(responseCode = "201", description = "Moduł został dodany")
  })
  @Operation(
      description = "Dodaje moduł do bazy danych, jeżeli nazwa będzie taka sama," +
          " jak element w bazie, zostanie on nadpisany"
  )
  public ResponseEntity<BasicResponseEntity> postModule(
      @RequestBody Module module
  ) {
    int id = confService.addModule(module);

    return ResponseEntity
        .created(URI.create("/api/conf/module-catalog"))
        .body(BasicResponseEntity.success(Integer.toString(id)));
  }

  @GetMapping("/module-categories")
  public ResponseEntity<ModuleCategory[]> getCategories() {
    return ResponseEntity.ok(ModuleCategory.values());
  }

  @GetMapping("/resource-types")
  public ResponseEntity<ResourceType[]> getResourceTypes() {
    return ResponseEntity.ok(ResourceType.values());
  }

  @GetMapping("/module-states")
  public ResponseEntity<ModuleState[]> getModuleStates() {
    return ResponseEntity.ok(ModuleState.values());
  }
}
