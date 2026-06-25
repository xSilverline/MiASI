package miasi.backend.domains.analysis.infrastructure.out.persistence;

import java.util.ArrayList;
import java.util.List;
import miasi.backend.database.JsonFileStorage;
import miasi.backend.domains.analysis.application.port.out.ISimulationDataProviderPort;
import miasi.backend.domains.analysis.application.port.out.ISimulationSessionRepositoryPort;
import miasi.backend.domains.analysis.domain._payload.PayloadOptimizationSession;
import miasi.backend.domains.analysis.domain._simulation.NominalSimulationSession;
import miasi.backend.domains.analysis.domain._simulation.ScenariosAnalysisSession;
import miasi.backend.domains.analysis.domain.core.ResourceType;
import miasi.backend.domains.analysis.domain.schedule.ImpactTarget;
import miasi.backend.domains.analysis.domain.schedule.ImpactType;
import miasi.backend.domains.analysis.domain.schedule.Threat;
import miasi.backend.domains.schedule.EventEffect;
import miasi.backend.domains.schedule.ScheduledEvent;
import miasi.backend.domains.schedule.application.port.out.ITimelineRepositoryPort;
import miasi.backend.domains.schedule.enums.EventType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

@Repository
public class SimulationSessionJsonAdapter
    implements ISimulationSessionRepositoryPort, ISimulationDataProviderPort {

  private final JsonFileStorage<NominalSimulationSession> nominalDb =
      new JsonFileStorage<>(NominalSimulationSession.class);
  private final JsonFileStorage<ScenariosAnalysisSession> scenariosDb =
      new JsonFileStorage<>(ScenariosAnalysisSession.class);
  private final JsonFileStorage<PayloadOptimizationSession> payloadDb =
      new JsonFileStorage<>(PayloadOptimizationSession.class);

  private final String nominalFile;
  private final String scenariosFile;
  private final String payloadFile;
  private final ITimelineRepositoryPort timelineRepository;

  private List<NominalSimulationSession> nominalSessions = new ArrayList<>();
  private List<ScenariosAnalysisSession> scenariosSessions = new ArrayList<>();

  public SimulationSessionJsonAdapter(
      @Value("${database.filename.analysis.nominal}") String nominalFile,
      @Value("${database.filename.analysis.scenarios}") String scenariosFile,
      @Value("${database.filename.analysis.payload}") String payloadFile,
      ITimelineRepositoryPort timelineRepository) {
    this.nominalFile = nominalFile;
    this.scenariosFile = scenariosFile;
    this.payloadFile = payloadFile;
    this.timelineRepository = timelineRepository;

    List<NominalSimulationSession> loadedNominal = nominalDb.loadListFromFile(nominalFile);
    if (loadedNominal != null) {
      this.nominalSessions = new ArrayList<>(loadedNominal);
    }

    List<ScenariosAnalysisSession> loadedScenarios = scenariosDb.loadListFromFile(scenariosFile);
    if (loadedScenarios != null) {
      this.scenariosSessions = new ArrayList<>(loadedScenarios);
    }
  }

  @Override
  public void saveNominal(NominalSimulationSession session) {
    nominalSessions.removeIf(s -> s.getId().equals(session.getId()));
    nominalSessions.add(session);
    nominalDb.saveListToFile(nominalSessions, nominalFile);
  }

  @Override
  public void saveScenarios(ScenariosAnalysisSession session) {
    scenariosSessions.removeIf(s -> s.getId().equals(session.getId()));
    scenariosSessions.add(session);
    scenariosDb.saveListToFile(scenariosSessions, scenariosFile);
  }

  @Override
  public PayloadOptimizationSession getPayloadSession(String payloadSessionId) {
    List<PayloadOptimizationSession> sessions = payloadDb.loadListFromFile(payloadFile);
    if (sessions == null) {
      return null;
    }

    return sessions.stream()
        .filter(s -> s.getId().equals(payloadSessionId))
        .findFirst()
        .orElse(null);
  }

  @Override
  public NominalSimulationSession getNominalSession(String nominalSessionId) {
    return nominalSessions.stream()
        .filter(s -> s.getId().equals(nominalSessionId))
        .findFirst()
        .orElse(null);
  }

  @Override
  public List<Threat> getThreatsForSchedule(String scheduleId) {
    return timelineRepository.findByType(EventType.THREAT).stream().map(this::toThreat).toList();
  }

  private Threat toThreat(ScheduledEvent event) {
    EventEffect effect = firstEffect(event);
    String target = effect == null ? "COLONY" : effect.getTarget();
    float value = effect == null ? 0f : (float) effect.getValue();

    return new Threat(event.getSol(), 1, impactType(effect), impactTarget(target), target, value);
  }

  private EventEffect firstEffect(ScheduledEvent event) {
    if (event.getEffects() == null || event.getEffects().isEmpty()) {
      return null;
    }
    return event.getEffects().get(0);
  }

  private ImpactType impactType(EventEffect effect) {
    if (effect == null) {
      return ImpactType.QUANTITY_CHANGE;
    }
    if ("PERCENT".equalsIgnoreCase(effect.getUnit())) {
      return ImpactType.EFFICIENCY_CHANGE;
    }
    return ImpactType.QUANTITY_CHANGE;
  }

  private ImpactTarget impactTarget(String target) {
    if (target == null || target.isBlank()) {
      return ImpactTarget.COLONY;
    }
    try {
      ResourceType.valueOf(target.trim().toUpperCase());
      return ImpactTarget.RESOURCE;
    } catch (IllegalArgumentException ignored) {
      return ImpactTarget.MODULE;
    }
  }
}
