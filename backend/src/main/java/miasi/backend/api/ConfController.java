package miasi.backend.api;

import lombok.RequiredArgsConstructor;
import miasi.backend.api.jsons.BasicResponseEntity;
import miasi.backend.domains.configuration.ConfContextProvider;
import miasi.backend.domains.configuration.missionPlan.MissionPlan;
import miasi.backend.domains.configuration.modules.Module;
import miasi.backend.domains.configuration.modules.ModuleCatalog;
import miasi.backend.domains.configuration.modules.ModuleType;
import miasi.backend.enums.ModuleState;
import miasi.backend.enums.ResourceType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@CrossOrigin(origins = "http://localhost:*")// TODO: do zmiany gdy będą znane porty frontendu
@RestController
@RequestMapping("/api/conf")
@RequiredArgsConstructor
public class ConfController {
  private final ConfContextProvider ctx;

  @GetMapping("/default/plan")
  public ResponseEntity<MissionPlan> getDefaultMissionPlan() {
    return ResponseEntity.ok(new MissionPlan());
  }

  @GetMapping("/{missionId}/plan")
  public ResponseEntity<MissionPlan> getMissionPlan(
      @PathVariable int missionId) {
    MissionPlan plan = ctx.getRepository().findById(missionId);
    if (plan == null)
      return ResponseEntity.notFound().build();
    return ResponseEntity.ok(plan);
  }

  @GetMapping("/module-catalog")
  public ResponseEntity<ModuleCatalog> getModuleCatalog() {
    return ResponseEntity.ok(ctx.getModuleCatalog());
  }

  @PostMapping("/plan")
  public ResponseEntity<BasicResponseEntity> postMissionPlan(
      @RequestBody MissionPlan missionPlan
  ) {
    int id = ctx.getRepository().save(missionPlan);
    missionPlan.throwCreatedEvent();
    return ResponseEntity
        .created(URI.create("/{%d}/module-catalog".formatted(id)))
        .body(BasicResponseEntity.success(Integer.toString(id)));
  }

  @PostMapping("/module")
  public ResponseEntity<BasicResponseEntity> postModule(
      @RequestBody Module module
  ) {
    return ResponseEntity.created(URI.create("/module-catalog")).body(BasicResponseEntity.success(Integer.toString(ctx.getModuleCatalog().add(module))));
  }

  @PostMapping("/module-type")
  public ResponseEntity<BasicResponseEntity> postModuleType(
      @RequestBody ModuleType type
  ) {
    return ResponseEntity.created(URI.create("/module-catalog")).body(BasicResponseEntity.success(Integer.toString(ctx.getModuleCatalog().add(type))));
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
