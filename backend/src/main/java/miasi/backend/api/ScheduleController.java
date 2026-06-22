package miasi.backend.api;

import jakarta.validation.Valid;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import miasi.backend.adapter.in.web.dto.BasicResponseEntity;
import miasi.backend.adapter.in.web.dto.CreateScheduleRequest;
import miasi.backend.adapter.in.web.dto.GenerateScenarioRequest;
import miasi.backend.adapter.in.web.dto.ScenarioDraftResponse;
import miasi.backend.adapter.in.web.dto.ScheduleModuleStateChangeRequest;
import miasi.backend.adapter.in.web.dto.ScheduleResponse;
import miasi.backend.adapter.in.web.dto.ScheduleResponseMapper;
import miasi.backend.adapter.in.web.dto.ScheduledEventRequest;
import miasi.backend.adapter.in.web.dto.TimelineResponse;
import miasi.backend.schedule.application.port.in.ApproveScenarioUseCase;
import miasi.backend.schedule.application.port.in.ChangeScheduleEventsUseCase;
import miasi.backend.schedule.application.port.in.CorrectScenarioUseCase;
import miasi.backend.schedule.application.port.in.CreateScheduleUseCase;
import miasi.backend.schedule.application.port.in.GenerateScenarioUseCase;
import miasi.backend.schedule.application.port.in.GetScheduleTimelineUseCase;
import miasi.backend.schedule.application.port.in.GetScheduleUseCase;
import miasi.backend.schedule.domain.EventType;
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

@CrossOrigin(origins = "http://localhost:*")
@RestController
@RequestMapping("/api/schedule")
@RequiredArgsConstructor
public class ScheduleController {

  private final CreateScheduleUseCase createScheduleUseCase;
  private final GetScheduleUseCase getScheduleUseCase;
  private final GetScheduleTimelineUseCase getScheduleTimelineUseCase;
  private final ChangeScheduleEventsUseCase changeScheduleEventsUseCase;
  private final GenerateScenarioUseCase generateScenarioUseCase;
  private final CorrectScenarioUseCase correctScenarioUseCase;
  private final ApproveScenarioUseCase approveScenarioUseCase;

  @PostMapping
  public ResponseEntity<ScheduleResponse> createSchedule(
      @Valid @RequestBody CreateScheduleRequest request) {
    var schedule =
        createScheduleUseCase.createSchedule(request.missionPlanId(), request.durationSols());

    return ResponseEntity.created(URI.create("/api/schedule/" + schedule.getId()))
        .body(ScheduleResponseMapper.toResponse(schedule));
  }

  @GetMapping("/{scheduleId}")
  public ResponseEntity<ScheduleResponse> getSchedule(@PathVariable String scheduleId) {
    return ResponseEntity.ok(
        ScheduleResponseMapper.toResponse(getScheduleUseCase.getSchedule(scheduleId)));
  }

  @GetMapping("/{scheduleId}/timeline")
  public ResponseEntity<TimelineResponse> getTimeline(
      @PathVariable String scheduleId, @RequestParam(required = false) EventType type) {
    var timeline = getScheduleTimelineUseCase.getTimeline(scheduleId);
    return ResponseEntity.ok(
        ScheduleResponseMapper.toResponse(type == null ? timeline : timeline.filterByType(type)));
  }

  @PostMapping("/{scheduleId}/events")
  public ResponseEntity<ScheduleResponse> addEvent(
      @PathVariable String scheduleId, @Valid @RequestBody ScheduledEventRequest request) {
    var schedule = changeScheduleEventsUseCase.addEvent(scheduleId, request.toDomain());
    return ResponseEntity.ok(ScheduleResponseMapper.toResponse(schedule));
  }

  @PutMapping("/{scheduleId}/events/{eventId}")
  public ResponseEntity<ScheduleResponse> updateEvent(
      @PathVariable String scheduleId,
      @PathVariable String eventId,
      @Valid @RequestBody ScheduledEventRequest request) {
    var schedule = changeScheduleEventsUseCase.updateEvent(scheduleId, eventId, request.toDomain());
    return ResponseEntity.ok(ScheduleResponseMapper.toResponse(schedule));
  }

  @PostMapping("/{scheduleId}/module-state-changes")
  public ResponseEntity<ScheduleResponse> scheduleModuleStateChange(
      @PathVariable String scheduleId,
      @Valid @RequestBody ScheduleModuleStateChangeRequest request) {
    var schedule =
        changeScheduleEventsUseCase.scheduleModuleStateChange(
            scheduleId,
            request.id(),
            request.sol(),
            request.description(),
            request.moduleId(),
            request.newState());
    return ResponseEntity.ok(ScheduleResponseMapper.toResponse(schedule));
  }

  @DeleteMapping("/{scheduleId}/events/{eventId}")
  public ResponseEntity<BasicResponseEntity> removeEvent(
      @PathVariable String scheduleId, @PathVariable String eventId) {
    changeScheduleEventsUseCase.removeEvent(scheduleId, eventId);
    return ResponseEntity.ok(BasicResponseEntity.success("Event removed"));
  }

  @PostMapping("/scenario")
  public ResponseEntity<ScenarioDraftResponse> generateScenario(
      @Valid @RequestBody GenerateScenarioRequest request) {
    var draft =
        generateScenarioUseCase.generateScenario(
            request.missionPlanId(), request.durationSols(), request.difficulty());

    return ResponseEntity.created(URI.create("/api/schedule/scenario/" + draft.getId()))
        .body(ScheduleResponseMapper.toResponse(draft));
  }

  @GetMapping("/scenario/{draftId}")
  public ResponseEntity<ScenarioDraftResponse> getScenarioDraft(@PathVariable String draftId) {
    return ResponseEntity.ok(
        ScheduleResponseMapper.toResponse(generateScenarioUseCase.getScenarioDraft(draftId)));
  }

  @PutMapping("/scenario/{draftId}/events/{eventId}")
  public ResponseEntity<ScenarioDraftResponse> correctScenarioEvent(
      @PathVariable String draftId,
      @PathVariable String eventId,
      @Valid @RequestBody ScheduledEventRequest request) {
    return ResponseEntity.ok(
        ScheduleResponseMapper.toResponse(
            correctScenarioUseCase.correctScenarioEvent(draftId, eventId, request.toDomain())));
  }

  @PostMapping("/scenario/{draftId}/approve")
  public ResponseEntity<ScheduleResponse> approveScenarioDraft(@PathVariable String draftId) {
    var schedule = approveScenarioUseCase.approveScenarioDraft(draftId);
    return ResponseEntity.created(URI.create("/api/schedule/" + schedule.getId()))
        .body(ScheduleResponseMapper.toResponse(schedule));
  }

  @PostMapping("/{scheduleId}/scenario/{draftId}/approve")
  public ResponseEntity<ScheduleResponse> approveScenarioIntoSchedule(
      @PathVariable String scheduleId, @PathVariable String draftId) {
    return ResponseEntity.ok(
        ScheduleResponseMapper.toResponse(
            approveScenarioUseCase.approveScenarioIntoSchedule(scheduleId, draftId)));
  }
}
