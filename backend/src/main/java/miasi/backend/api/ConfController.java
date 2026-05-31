package miasi.backend.api;

import miasi.backend.domains.configuration.MissionPlan;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "http://localhost:*")// TODO: do zmiany gdy będą znane porty frontendu
@RestController
@RequestMapping("/api/conf")
public class ConfController {
  @GetMapping("/{missionId}/plan")
  public ResponseEntity<MissionPlan> getMissionPlan(
      @PathVariable String missionId) {
    return ResponseEntity.ok(new MissionPlan());
  }
}
