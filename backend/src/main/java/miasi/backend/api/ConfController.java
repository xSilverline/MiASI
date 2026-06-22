package miasi.backend.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.OptionalInt;
import lombok.RequiredArgsConstructor;
import miasi.backend.adapter.in.web.dto.BasicResponseEntity;
import miasi.backend.adapter.in.web.dto.ConfigurationRequestMapper;
import miasi.backend.adapter.in.web.dto.MissionPlanRequest;
import miasi.backend.adapter.in.web.dto.ModuleRequest;
import miasi.backend.adapter.in.web.dto.ModuleTypeRequest;
import miasi.backend.configuration.application.port.in.GetMissionPlanUseCase;
import miasi.backend.configuration.application.port.in.GetModuleCatalogUseCase;
import miasi.backend.configuration.application.port.in.ManageMissionPlanUseCase;
import miasi.backend.configuration.application.port.in.ManageModuleCatalogUseCase;
import miasi.backend.domains.configuration.missionPlan.MissionPlan;
import miasi.backend.domains.configuration.modules.ModuleCatalog;
import miasi.backend.sharedkernel.model.ModuleState;
import miasi.backend.sharedkernel.model.ResourceType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "http://localhost:*") // TODO: do zmiany gdy będą znane porty frontendu
@RestController
@RequestMapping("/api/conf")
@RequiredArgsConstructor
public class ConfController {

  private final GetMissionPlanUseCase getMissionPlanUseCase;
  private final ManageMissionPlanUseCase manageMissionPlanUseCase;
  private final GetModuleCatalogUseCase getModuleCatalogUseCase;
  private final ManageModuleCatalogUseCase manageModuleCatalogUseCase;

  @GetMapping("/default/plan")
  public ResponseEntity<MissionPlan> getDefaultMissionPlan() {
    return ResponseEntity.ok(getMissionPlanUseCase.getDefaultMissionPlan());
  }

  @GetMapping("/{missionId}/plan")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Plan misji został znaleziony"),
    @ApiResponse(
        responseCode = "404",
        description = "Nie znaleziono planu misji o podanym id",
        content = @Content)
  })
  @Operation(
      summary = "Pobiera plan misji o podanym id",
      description =
          "Plany misji mają id w przedziale [0;X), gdzie X to wynik zapytania /api/conf/plans-count")
  public ResponseEntity<MissionPlan> getMissionPlan(@PathVariable int missionId) {
    Optional<MissionPlan> plan = getMissionPlanUseCase.getMissionPlan(missionId);
    return plan.map(ResponseEntity::ok)
        .orElseThrow(() -> new NoSuchElementException("Mission plan not found: " + missionId));
  }

  @GetMapping("/module-catalog")
  public ResponseEntity<ModuleCatalog> getModuleCatalog() {
    return ResponseEntity.ok(getModuleCatalogUseCase.getModuleCatalog());
  }

  @Operation(
      summary = "Wysyła do bazy danych nowy plan misji",
      description =
          "Jeżeli parametr 'override' jest ustawiony, to plan nadpisze istniejacy plan na podanym id."
              + " Jeżeli podano błedne id, zwrócony zostaje komunikat NOT FOUND."
              + " Zwraca id utworzonego/nadpisanego planu jako 'message'")
  @ApiResponses({
    @ApiResponse(responseCode = "201", description = "Plan został utworzony"),
    @ApiResponse(
        responseCode = "404",
        description = "Nie znaleziono planu do nadpisania",
        content = @Content)
  })
  @PostMapping("/plan")
  public ResponseEntity<BasicResponseEntity> postMissionPlan(
      @Valid @RequestBody MissionPlanRequest request,
      @RequestParam(required = false) Integer override) {
    MissionPlan missionPlan = ConfigurationRequestMapper.toDomain(request);
    int id;
    if (override != null) {
      OptionalInt overrideId = manageMissionPlanUseCase.overrideMissionPlan(override, missionPlan);
      if (overrideId.isEmpty()) {
        throw new NoSuchElementException("Mission plan not found: " + override);
      }
      id = overrideId.getAsInt();
    } else {
      id = manageMissionPlanUseCase.saveMissionPlan(missionPlan);
    }

    return ResponseEntity.created(URI.create("/api/conf/%d/plan".formatted(id)))
        .body(BasicResponseEntity.success(Integer.toString(id)));
  }

  @GetMapping("plans-count")
  @Operation(description = "Zwraca ilość planów misji w bazie danych w polu 'message'")
  public ResponseEntity<BasicResponseEntity> getMissionsCount() {
    return ResponseEntity.ok()
        .body(BasicResponseEntity.success(Integer.toString(getMissionPlanUseCase.getPlansCount())));
  }

  @PostMapping("/module")
  @ApiResponses({@ApiResponse(responseCode = "201", description = "Moduł został dodany")})
  @Operation(
      description =
          "Dodaje moduł do bazy danych, jeżeli nazwa będzie taka sama,"
              + " jak element w bazie, zostanie on nadpisany")
  public ResponseEntity<BasicResponseEntity> postModule(@Valid @RequestBody ModuleRequest request) {
    int id = manageModuleCatalogUseCase.addModule(ConfigurationRequestMapper.toDomain(request));

    return ResponseEntity.created(URI.create("/api/conf/module-catalog"))
        .body(BasicResponseEntity.success(Integer.toString(id)));
  }

  @PostMapping("/module-type")
  @ApiResponses({@ApiResponse(responseCode = "201", description = "Typ modułu został dodany")})
  @Operation(
      description =
          "Dodaje typ moduły do bazy danych, jeżeli nazwa będzie taka sama,"
              + " jak element w bazie, zostanie on nadpisany")
  public ResponseEntity<BasicResponseEntity> postModuleType(
      @Valid @RequestBody ModuleTypeRequest request) {
    int id = manageModuleCatalogUseCase.addModuleType(ConfigurationRequestMapper.toDomain(request));

    return ResponseEntity.created(URI.create("/api/conf/module-catalog"))
        .body(BasicResponseEntity.success(Integer.toString(id)));
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
