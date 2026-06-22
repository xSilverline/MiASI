package miasi.backend.api;

import lombok.RequiredArgsConstructor;
import miasi.backend.api.jsons.BasicResponseEntity;
import miasi.backend.api.jsons.CreateScheduleRequest;
import miasi.backend.api.jsons.GenerateScenarioRequest;
import miasi.backend.api.jsons.ScheduleModuleStateChangeRequest;
import miasi.backend.domains.schedule.MissionSchedule;
import miasi.backend.domains.schedule.MissionTimeline;
import miasi.backend.domains.schedule.ScenarioDraft;
import miasi.backend.domains.schedule.ScheduleService;
import miasi.backend.domains.schedule.ScheduledEvent;
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

@CrossOrigin(origins = "http://localhost:*")
@RestController
@RequestMapping("/api/schedule")
@RequiredArgsConstructor
public class ScheduleController {

  private final ScheduleService scheduleService;

  @PostMapping
  public ResponseEntity<MissionSchedule> createSchedule(
      @RequestBody CreateScheduleRequest request) {
    MissionSchedule schedule =
        scheduleService.createSchedule(request.missionPlanId(), request.durationSols());

    return ResponseEntity.created(URI.create("/api/schedule/" + schedule.getId())).body(schedule);
  }

  @GetMapping("/{scheduleId}")
  public ResponseEntity<MissionSchedule> getSchedule(@PathVariable String scheduleId) {
    return ResponseEntity.ok(scheduleService.getSchedule(scheduleId));
  }

  @GetMapping("/{scheduleId}/timeline")
  public ResponseEntity<MissionTimeline> getTimeline(
      @PathVariable String scheduleId, @RequestParam(required = false) EventType type) {
    MissionTimeline timeline = scheduleService.getTimeline(scheduleId);
    return ResponseEntity.ok(type == null ? timeline : timeline.filterByType(type));
  }

  @PostMapping("/{scheduleId}/events")
  public ResponseEntity<MissionSchedule> addEvent(
      @PathVariable String scheduleId, @RequestBody ScheduledEvent event) {
    MissionSchedule schedule = scheduleService.addEvent(scheduleId, event);
    return ResponseEntity.ok(schedule);
  }

  @PutMapping("/{scheduleId}/events/{eventId}")
  public ResponseEntity<MissionSchedule> updateEvent(
      @PathVariable String scheduleId,
      @PathVariable String eventId,
      @RequestBody ScheduledEvent event) {
    MissionSchedule schedule = scheduleService.updateEvent(scheduleId, eventId, event);
    return ResponseEntity.ok(schedule);
  }

  @PostMapping("/{scheduleId}/module-state-changes")
  public ResponseEntity<MissionSchedule> scheduleModuleStateChange(
      @PathVariable String scheduleId, @RequestBody ScheduleModuleStateChangeRequest request) {
    MissionSchedule schedule =
        scheduleService.scheduleModuleStateChange(
            scheduleId,
            request.id(),
            request.sol(),
            request.description(),
            request.moduleId(),
            request.newState());
    return ResponseEntity.ok(schedule);
  }

  @DeleteMapping("/{scheduleId}/events/{eventId}")
  public ResponseEntity<BasicResponseEntity> removeEvent(
      @PathVariable String scheduleId, @PathVariable String eventId) {
    scheduleService.removeEvent(scheduleId, eventId);
    return ResponseEntity.ok(BasicResponseEntity.success("Event removed"));
  }

  @PostMapping("/scenario")
  public ResponseEntity<ScenarioDraft> generateScenario(
      @RequestBody GenerateScenarioRequest request) {
    ScenarioDraft draft =
        scheduleService.generateScenario(
            request.missionPlanId(), request.durationSols(), request.difficulty());

    return ResponseEntity.created(URI.create("/api/schedule/scenario/" + draft.getId()))
        .body(draft);
  }

  @GetMapping("/scenario/{draftId}")
  public ResponseEntity<ScenarioDraft> getScenarioDraft(@PathVariable String draftId) {
    return ResponseEntity.ok(scheduleService.getScenarioDraft(draftId));
  }

  @PutMapping("/scenario/{draftId}/events/{eventId}")
  public ResponseEntity<ScenarioDraft> correctScenarioEvent(
      @PathVariable String draftId,
      @PathVariable String eventId,
      @RequestBody ScheduledEvent event) {
    return ResponseEntity.ok(scheduleService.correctScenarioEvent(draftId, eventId, event));
  }

  @PostMapping("/scenario/{draftId}/approve")
  public ResponseEntity<MissionSchedule> approveScenarioDraft(@PathVariable String draftId) {
    MissionSchedule schedule = scheduleService.approveScenarioDraft(draftId);
    return ResponseEntity.created(URI.create("/api/schedule/" + schedule.getId())).body(schedule);
  }

  @PostMapping("/{scheduleId}/scenario/{draftId}/approve")
  public ResponseEntity<MissionSchedule> approveScenarioIntoSchedule(
      @PathVariable String scheduleId, @PathVariable String draftId) {
    return ResponseEntity.ok(scheduleService.approveScenarioIntoSchedule(scheduleId, draftId));
  }
}
