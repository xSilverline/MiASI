package miasi.backend.api;

import lombok.RequiredArgsConstructor;
import miasi.backend.api.jsons.BasicResponseEntity;
import miasi.backend.domains.configuration.ConfService;
import miasi.backend.domains.configuration.missionPlan.MissionPlan;
import miasi.backend.domains.configuration.modules.Module;
import miasi.backend.domains.configuration.modules.ModuleCatalog;
import miasi.backend.domains.configuration.modules.ModuleType;
import miasi.backend.enums.ModuleState;
import miasi.backend.enums.ResourceType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

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
  public ResponseEntity<MissionPlan> getMissionPlan(@PathVariable int missionId) {
    MissionPlan plan = confService.getMissionPlan(missionId);
    return plan != null ? ResponseEntity.ok(plan) : ResponseEntity.notFound().build();
  }

  @GetMapping("/module-catalog")
  public ResponseEntity<ModuleCatalog> getModuleCatalog() {
    return ResponseEntity.ok(confService.getModuleCatalog());
  }

  @PostMapping("/plan")
  public ResponseEntity<BasicResponseEntity> postMissionPlan(
      @RequestBody MissionPlan missionPlan
  ) {
    int id = confService.saveMissionPlan(missionPlan);

    return ResponseEntity
        .created(URI.create("/api/conf/%d/plan".formatted(id)))
        .body(BasicResponseEntity.success(Integer.toString(id)));
  }

  @PostMapping("/module")
  public ResponseEntity<BasicResponseEntity> postModule(
      @RequestBody Module module
  ) {
    int id = confService.addModule(module);

    return ResponseEntity
        .created(URI.create("/api/conf/module-catalog"))
        .body(BasicResponseEntity.success(Integer.toString(id)));
  }

  @PostMapping("/module-type")
  public ResponseEntity<BasicResponseEntity> postModuleType(
      @RequestBody ModuleType type
  ) {
    int id = confService.addModuleType(type);

    return ResponseEntity
        .created(URI.create("/api/conf/module-catalog"))
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