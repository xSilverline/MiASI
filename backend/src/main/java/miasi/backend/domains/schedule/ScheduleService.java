package miasi.backend.domains.schedule;

import miasi.backend.domains.schedule.enums.DifficultyLevel;
import miasi.backend.domains.schedule.enums.EventType;
import miasi.backend.domains.schedule.enums.ThreatType;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ScheduleService {

  private final Map<String, MissionSchedule> schedules = new ConcurrentHashMap<>();
  private final Map<String, ScenarioDraft> scenarioDrafts = new ConcurrentHashMap<>();
  private final EventSchedulingPolicy policy = new EventSchedulingPolicy();
  private final ThreatDictionary threatDictionary = defaultThreatDictionary();

  public MissionSchedule createSchedule(String missionPlanId, int durationSols) {
    MissionSchedule schedule = MissionSchedule.createDraft(missionPlanId, durationSols);
    schedules.put(schedule.getId(), schedule);
    return schedule;
  }

  public MissionSchedule getSchedule(String scheduleId) {
    MissionSchedule schedule = schedules.get(scheduleId);
    if (schedule == null) {
      throw new NoSuchElementException("Mission schedule not found: " + scheduleId);
    }
    return schedule;
  }

  public MissionTimeline getTimeline(String scheduleId) {
    return getSchedule(scheduleId).timeline();
  }

  public MissionSchedule addEvent(String scheduleId, ScheduledEvent event) {
    MissionSchedule schedule = getSchedule(scheduleId);
    validateEventScheduling(schedule, event);
    schedule.addEvent(event);
    return schedule;
  }

  public MissionSchedule updateEvent(String scheduleId, String eventId, ScheduledEvent event) {
    MissionSchedule schedule = getSchedule(scheduleId);
    validateEventScheduling(schedule, event);
    schedule.updateEvent(eventId, event);
    return schedule;
  }

  public MissionSchedule scheduleModuleStateChange(
      String scheduleId,
      String eventId,
      int sol,
      String description,
      String moduleId,
      ModuleState newState) {
    ModuleStateChange stateChange = new ModuleStateChange(moduleId, newState);
    stateChange.setId(hasText(eventId) ? eventId : UUID.randomUUID().toString());
    stateChange.setType(EventType.MODULE_STATE_CHANGE);
    stateChange.setSol(sol);
    stateChange.setDescription(description);
    validateModuleStateChange(stateChange);
    return addEvent(scheduleId, stateChange);
  }

  public void removeEvent(String scheduleId, String eventId) {
    getSchedule(scheduleId).removeEvent(eventId);
  }

  public ScenarioDraft generateScenario(
      String missionPlanId, int durationSols, DifficultyLevel difficulty) {
    ScenarioGenerator generator = new ScenarioGenerator(missionPlanId, threatDictionary, null);
    ScenarioDraft draft = generator.generate(missionPlanId, durationSols, difficulty);
    scenarioDrafts.put(draft.getId(), draft);
    return draft;
  }

  public ScenarioDraft getScenarioDraft(String draftId) {
    ScenarioDraft draft = scenarioDrafts.get(draftId);
    if (draft == null) {
      throw new NoSuchElementException("Scenario draft not found: " + draftId);
    }
    return draft;
  }

  public ScenarioDraft correctScenarioEvent(
      String draftId, String eventId, ScheduledEvent correctedEvent) {
    ScenarioDraft draft = getScenarioDraft(draftId);
    draft.correctEvent(eventId, correctedEvent);
    return draft;
  }

  public MissionSchedule approveScenarioDraft(String draftId) {
    ScenarioDraft draft = getScenarioDraft(draftId);
    MissionSchedule schedule = draft.approve();
    schedules.put(schedule.getId(), schedule);
    return schedule;
  }

  public MissionSchedule approveScenarioIntoSchedule(String scheduleId, String draftId) {
    MissionSchedule schedule = getSchedule(scheduleId);
    ScenarioDraft draft = getScenarioDraft(draftId);
    schedule.approveScenario(draft);
    return schedule;
  }

  private ThreatDictionary defaultThreatDictionary() {
    return new ThreatDictionary(
        List.of(
            new ThreatDefinition(
                ThreatType.DUST_STORM,
                DifficultyLevel.LEVEL_I,
                "solar-panels",
                1.0,
                2.0,
                "days"),
            new ThreatDefinition(
                ThreatType.MODULE_FAILURE,
                DifficultyLevel.LEVEL_II,
                "habitat-module",
                2.0,
                4.0,
                "days"),
            new ThreatDefinition(
                ThreatType.RESOURCE_LOSS,
                DifficultyLevel.LEVEL_III,
                "water-storage",
                3.0,
                6.0,
                "percent"),
            new ThreatDefinition(
                ThreatType.PRODUCTION_DISRUPTION,
                DifficultyLevel.LEVEL_IV,
                "food-production",
                4.0,
                8.0,
                "days"),
            new ThreatDefinition(
                ThreatType.MODULE_FAILURE,
                DifficultyLevel.LEVEL_V,
                "life-support",
                6.0,
                10.0,
                "days")));
  }

  private void validateEventScheduling(MissionSchedule schedule, ScheduledEvent event) {
    policy.validateSolWithinMission(event, schedule.getDurationSols());
    if (!policy.allowManyEventsInSameSol(event, schedule)) {
      throw new IllegalArgumentException("Multiple events in the same sol are not allowed");
    }
    if (event instanceof SupplyDelivery delivery) {
      policy.validateDeliveryWeight(delivery, Double.MAX_VALUE);
    }
  }

  private void validateModuleStateChange(ModuleStateChange stateChange) {
    if (!hasText(stateChange.getModuleId())) {
      throw new IllegalArgumentException("Module id is required");
    }
    if (stateChange.getNewState() == null) {
      throw new IllegalArgumentException("New module state is required");
    }
  }

  private boolean hasText(String value) {
    return value != null && !value.isBlank();
  }
}
