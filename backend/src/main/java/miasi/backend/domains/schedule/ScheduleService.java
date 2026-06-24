package miasi.backend.domains.schedule;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import miasi.backend.domains.schedule.enums.DifficultyLevel;
import miasi.backend.domains.schedule.enums.EventType;
import miasi.backend.domains.schedule.enums.ThreatType;
import miasi.backend.domains.schedule.ports.ScheduleRepositoryPort;

public class ScheduleService {

  private final Map<String, MissionSchedule> schedules = new ConcurrentHashMap<>();
  private final Map<String, ScenarioDraft> scenarioDrafts = new ConcurrentHashMap<>();
  private final EventSchedulingPolicy policy = new EventSchedulingPolicy();
  private final ThreatDictionary threatDictionary = defaultThreatDictionary();
  private final ScheduleRepositoryPort scheduleRepository;

  public ScheduleService() {
    this(null);
  }

  public ScheduleService(ScheduleRepositoryPort scheduleRepository) {
    this.scheduleRepository = scheduleRepository;
  }

  public MissionSchedule createSchedule(String missionPlanId, int durationSols) {
    MissionSchedule schedule = MissionSchedule.createDraft(missionPlanId, durationSols);
    persistSchedule(schedule);
    return schedule;
  }

  public MissionSchedule getSchedule(String scheduleId) {
    MissionSchedule schedule = schedules.get(scheduleId);
    if (schedule == null && scheduleRepository != null) {
      schedule = scheduleRepository.findScheduleById(scheduleId).orElse(null);
      if (schedule != null) {
        schedules.put(schedule.getId(), schedule);
      }
    }
    if (schedule == null) {
      throw new NoSuchElementException("Mission schedule not found: " + scheduleId);
    }
    return schedule;
  }

  public MissionTimeline getTimeline(String scheduleId) {
    return getSchedule(scheduleId).timeline();
  }

  public List<ScheduledEvent> getScheduleEvents(String scheduleId) {
    return getTimeline(scheduleId).getEventsSortedBySol();
  }

  public MissionSchedule addEvent(String scheduleId, ScheduledEvent event) {
    MissionSchedule schedule = getSchedule(scheduleId);
    validateEventScheduling(schedule, event);
    schedule.addEvent(event);
    persistSchedule(schedule);
    return schedule;
  }

  public MissionSchedule updateEvent(String scheduleId, String eventId, ScheduledEvent event) {
    MissionSchedule schedule = getSchedule(scheduleId);
    validateEventScheduling(schedule, event);
    schedule.updateEvent(eventId, event);
    persistSchedule(schedule);
    return schedule;
  }

  public MissionSchedule replaceEvents(String scheduleId, List<ScheduledEvent> events) {
    MissionSchedule schedule = getSchedule(scheduleId);
    List<ScheduledEvent> replacement = events == null ? List.of() : events;
    MissionSchedule validatedSchedule =
        new MissionSchedule(
            schedule.getId(),
            schedule.getMissionPlanId(),
            schedule.getDurationSols(),
            schedule.getStatus(),
            new ArrayList<>());

    replacement.forEach(
        event -> {
          validateEventScheduling(validatedSchedule, event);
          validatedSchedule.addEvent(event);
        });

    schedule.setEvents(new ArrayList<>(replacement));
    persistSchedule(schedule);
    return schedule;
  }

  public MissionSchedule replaceEventsAtSol(
      String scheduleId, int sol, List<ScheduledEvent> replacementEvents) {
    MissionSchedule schedule = getSchedule(scheduleId);
    List<ScheduledEvent> mergedEvents =
        schedule.getEvents() == null
            ? new ArrayList<>()
            : schedule.getEvents().stream()
                .filter(event -> event != null && event.getSol() != sol)
                .collect(java.util.stream.Collectors.toCollection(ArrayList::new));
    if (replacementEvents != null) {
      mergedEvents.addAll(replacementEvents);
    }
    return replaceEvents(scheduleId, mergedEvents);
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
    MissionSchedule schedule = getSchedule(scheduleId);
    schedule.removeEvent(eventId);
    persistSchedule(schedule);
  }

  public ScenarioDraft generateScenario(
      String missionPlanId, int durationSols, DifficultyLevel difficulty) {
    ScenarioGenerator generator = new ScenarioGenerator(missionPlanId, threatDictionary, null);
    ScenarioDraft draft = generator.generate(missionPlanId, durationSols, difficulty);
    persistScenarioDraft(draft);
    return draft;
  }

  public ScenarioDraft getScenarioDraft(String draftId) {
    ScenarioDraft draft = scenarioDrafts.get(draftId);
    if (draft == null && scheduleRepository != null) {
      draft = scheduleRepository.findScenarioDraftById(draftId).orElse(null);
      if (draft != null) {
        scenarioDrafts.put(draft.getId(), draft);
      }
    }
    if (draft == null) {
      throw new NoSuchElementException("Scenario draft not found: " + draftId);
    }
    return draft;
  }

  public List<ScheduledEvent> getScenarioEvents(String draftId) {
    ScenarioDraft draft = getScenarioDraft(draftId);
    List<ScheduledEvent> events = draft.getProposedEvents();
    return events == null ? List.of() : List.copyOf(events);
  }

  public ScenarioDraft addScenarioEvent(String draftId, ScheduledEvent event) {
    ScenarioDraft draft = getScenarioDraft(draftId);
    draft.addEvent(event);
    persistScenarioDraft(draft);
    return draft;
  }

  public ScenarioDraft replaceScenarioEvents(String draftId, List<ScheduledEvent> events) {
    ScenarioDraft draft = getScenarioDraft(draftId);
    List<ScheduledEvent> replacement = events == null ? List.of() : events;
    replacement.forEach(event -> policy.validateSolWithinMission(event, draft.getDurationSols()));
    draft.setProposedEvents(new ArrayList<>(replacement));
    persistScenarioDraft(draft);
    return draft;
  }

  public ScenarioDraft replaceScenarioEventsAtSol(
      String draftId, int sol, List<ScheduledEvent> replacementEvents) {
    ScenarioDraft draft = getScenarioDraft(draftId);
    List<ScheduledEvent> mergedEvents =
        draft.getProposedEvents() == null
            ? new ArrayList<>()
            : draft.getProposedEvents().stream()
                .filter(event -> event != null && event.getSol() != sol)
                .collect(java.util.stream.Collectors.toCollection(ArrayList::new));
    if (replacementEvents != null) {
      mergedEvents.addAll(replacementEvents);
    }
    return replaceScenarioEvents(draftId, mergedEvents);
  }

  public void removeScenarioEvent(String draftId, String eventId) {
    ScenarioDraft draft = getScenarioDraft(draftId);
    draft.removeEvent(eventId);
    persistScenarioDraft(draft);
  }

  public ScenarioDraft correctScenarioEvent(
      String draftId, String eventId, ScheduledEvent correctedEvent) {
    ScenarioDraft draft = getScenarioDraft(draftId);
    draft.correctEvent(eventId, correctedEvent);
    persistScenarioDraft(draft);
    return draft;
  }

  public MissionSchedule approveScenarioDraft(String draftId) {
    ScenarioDraft draft = getScenarioDraft(draftId);
    MissionSchedule schedule = draft.approve();
    persistSchedule(schedule);
    return schedule;
  }

  public MissionSchedule approveScenarioIntoSchedule(String scheduleId, String draftId) {
    MissionSchedule schedule = getSchedule(scheduleId);
    ScenarioDraft draft = getScenarioDraft(draftId);
    schedule.approveScenario(draft);
    persistSchedule(schedule);
    return schedule;
  }

  private void persistSchedule(MissionSchedule schedule) {
    schedules.put(schedule.getId(), schedule);
    if (scheduleRepository != null) {
      scheduleRepository.saveSchedule(schedule);
    }
  }

  private void persistScenarioDraft(ScenarioDraft draft) {
    scenarioDrafts.put(draft.getId(), draft);
    if (scheduleRepository != null) {
      scheduleRepository.saveScenarioDraft(draft);
    }
  }

  private ThreatDictionary defaultThreatDictionary() {
    return new ThreatDictionary(
        List.of(
            new ThreatDefinition(
                ThreatType.DUST_STORM,
                DifficultyLevel.LEVEL_I,
                "solar-panels",
                "reduced solar energy production",
                1.0,
                2.0,
                "days"),
            new ThreatDefinition(
                ThreatType.MODULE_FAILURE,
                DifficultyLevel.LEVEL_II,
                "habitat-module",
                "limited habitat availability",
                2.0,
                4.0,
                "days"),
            new ThreatDefinition(
                ThreatType.RESOURCE_LOSS,
                DifficultyLevel.LEVEL_III,
                "water-storage",
                "water reserves decrease",
                3.0,
                6.0,
                "percent"),
            new ThreatDefinition(
                ThreatType.PRODUCTION_DISRUPTION,
                DifficultyLevel.LEVEL_IV,
                "food-production",
                "food production is interrupted",
                4.0,
                8.0,
                "days"),
            new ThreatDefinition(
                ThreatType.MODULE_FAILURE,
                DifficultyLevel.LEVEL_V,
                "life-support",
                "life support capacity drops",
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
