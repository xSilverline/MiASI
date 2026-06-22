package miasi.backend.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import miasi.backend.api.jsons.BasicResponseEntity;
import miasi.backend.domains.configuration.ConfService;
import miasi.backend.domains.configuration.missionPlan.MissionPlan;
import miasi.backend.domains.configuration.modules.Module;
import miasi.backend.domains.configuration.modules.ModuleCategory;
import miasi.backend.enums.ModuleState;
import miasi.backend.enums.ResourceType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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