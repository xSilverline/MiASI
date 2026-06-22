package miasi.backend.schedule.application.service;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import miasi.backend.common.domain.model.ModuleState;
import miasi.backend.schedule.application.port.in.ApproveScenarioUseCase;
import miasi.backend.schedule.application.port.in.ChangeScheduleEventsUseCase;
import miasi.backend.schedule.application.port.in.CorrectScenarioUseCase;
import miasi.backend.schedule.application.port.in.CreateScheduleUseCase;
import miasi.backend.schedule.application.port.in.GenerateScenarioUseCase;
import miasi.backend.schedule.application.port.in.GetScheduleTimelineUseCase;
import miasi.backend.schedule.application.port.in.GetScheduleUseCase;
import miasi.backend.schedule.application.port.out.MissionScheduleRepositoryPort;
import miasi.backend.schedule.application.port.out.ScheduleEventPublisherPort;
import miasi.backend.schedule.domain.model.DifficultyLevel;
import miasi.backend.schedule.domain.model.EventType;
import miasi.backend.schedule.domain.model.MissionSchedule;
import miasi.backend.schedule.domain.model.MissionTimeline;
import miasi.backend.schedule.domain.model.ModuleStateChange;
import miasi.backend.schedule.domain.model.ScenarioDraft;
import miasi.backend.schedule.domain.model.ScheduledEvent;
import miasi.backend.schedule.domain.model.SupplyDelivery;
import miasi.backend.schedule.domain.model.ThreatDefinition;
import miasi.backend.schedule.domain.model.ThreatType;
import miasi.backend.schedule.domain.service.EventSchedulingPolicy;
import miasi.backend.schedule.domain.service.ScenarioGenerator;
import miasi.backend.schedule.domain.service.ThreatDictionary;

public class ScheduleApplicationService
    implements CreateScheduleUseCase,
        GetScheduleUseCase,
        GetScheduleTimelineUseCase,
        ChangeScheduleEventsUseCase,
        GenerateScenarioUseCase,
        CorrectScenarioUseCase,
        ApproveScenarioUseCase {

  private final MissionScheduleRepositoryPort scheduleRepository;
  private final Map<String, ScenarioDraft> scenarioDrafts = new ConcurrentHashMap<>();
  private final EventSchedulingPolicy policy = new EventSchedulingPolicy();
  private final ThreatDictionary threatDictionary = defaultThreatDictionary();
  private final ScheduleEventPublisherPort eventPublisher;

  public ScheduleApplicationService() {
    this(ScheduleEventPublisherPort.NO_OP);
  }

  public ScheduleApplicationService(ScheduleEventPublisherPort eventPublisher) {
    this(new DefaultInMemoryScheduleRepository(), eventPublisher);
  }

  public ScheduleApplicationService(
      MissionScheduleRepositoryPort scheduleRepository, ScheduleEventPublisherPort eventPublisher) {
    this.scheduleRepository = scheduleRepository;
    this.eventPublisher =
        eventPublisher == null ? ScheduleEventPublisherPort.NO_OP : eventPublisher;
  }

  @Override
  public MissionSchedule createSchedule(String missionPlanId, int durationSols) {
    MissionSchedule schedule = MissionSchedule.createDraft(missionPlanId, durationSols);
    scheduleRepository.save(schedule);
    eventPublisher.publishScheduleCreated(schedule);
    return schedule;
  }

  @Override
  public MissionSchedule getSchedule(String scheduleId) {
    return scheduleRepository
        .findById(scheduleId)
        .orElseThrow(() -> new NoSuchElementException("Mission schedule not found: " + scheduleId));
  }

  @Override
  public MissionTimeline getTimeline(String scheduleId) {
    return getSchedule(scheduleId).timeline();
  }

  @Override
  public MissionSchedule addEvent(String scheduleId, ScheduledEvent event) {
    MissionSchedule schedule = getSchedule(scheduleId);
    validateEventScheduling(schedule, event);
    schedule.addEvent(event);
    scheduleRepository.save(schedule);
    eventPublisher.publishScheduledEventAdded(scheduleId, event);
    eventPublisher.publishScheduleUpdated(schedule);
    return schedule;
  }

  @Override
  public MissionSchedule updateEvent(String scheduleId, String eventId, ScheduledEvent event) {
    MissionSchedule schedule = getSchedule(scheduleId);
    validateEventScheduling(schedule, event);
    schedule.updateEvent(eventId, event);
    scheduleRepository.save(schedule);
    eventPublisher.publishScheduleUpdated(schedule);
    return schedule;
  }

  @Override
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

  @Override
  public void removeEvent(String scheduleId, String eventId) {
    MissionSchedule schedule = getSchedule(scheduleId);
    schedule.removeEvent(eventId);
    scheduleRepository.save(schedule);
    eventPublisher.publishScheduleUpdated(schedule);
  }

  @Override
  public ScenarioDraft generateScenario(
      String missionPlanId, int durationSols, DifficultyLevel difficulty) {
    ScenarioGenerator generator = new ScenarioGenerator(missionPlanId, threatDictionary, null);
    ScenarioDraft draft = generator.generate(missionPlanId, durationSols, difficulty);
    scenarioDrafts.put(draft.getId(), draft);
    return draft;
  }

  @Override
  public ScenarioDraft getScenarioDraft(String draftId) {
    ScenarioDraft draft = scenarioDrafts.get(draftId);
    if (draft == null) {
      throw new NoSuchElementException("Scenario draft not found: " + draftId);
    }
    return draft;
  }

  @Override
  public ScenarioDraft correctScenarioEvent(
      String draftId, String eventId, ScheduledEvent correctedEvent) {
    ScenarioDraft draft = getScenarioDraft(draftId);
    draft.correctEvent(eventId, correctedEvent);
    return draft;
  }

  @Override
  public MissionSchedule approveScenarioDraft(String draftId) {
    ScenarioDraft draft = getScenarioDraft(draftId);
    MissionSchedule schedule = draft.approve();
    scheduleRepository.save(schedule);
    eventPublisher.publishScheduleCreated(schedule);
    return schedule;
  }

  @Override
  public MissionSchedule approveScenarioIntoSchedule(String scheduleId, String draftId) {
    MissionSchedule schedule = getSchedule(scheduleId);
    ScenarioDraft draft = getScenarioDraft(draftId);
    schedule.approveScenario(draft);
    scheduleRepository.save(schedule);
    eventPublisher.publishScheduleUpdated(schedule);
    return schedule;
  }

  private ThreatDictionary defaultThreatDictionary() {
    return new ThreatDictionary(
        List.of(
            new ThreatDefinition(
                ThreatType.DUST_STORM, DifficultyLevel.LEVEL_I, "solar-panels", 1.0, 2.0, "days"),
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

  private static final class DefaultInMemoryScheduleRepository
      implements MissionScheduleRepositoryPort {
    private final ConcurrentMap<String, MissionSchedule> schedules = new ConcurrentHashMap<>();

    @Override
    public void save(MissionSchedule schedule) {
      schedules.put(schedule.getId(), schedule);
    }

    @Override
    public java.util.Optional<MissionSchedule> findById(String scheduleId) {
      return java.util.Optional.ofNullable(schedules.get(scheduleId));
    }

    @Override
    public void delete(String scheduleId) {
      schedules.remove(scheduleId);
    }
  }
}
