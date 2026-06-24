package miasi.backend.domains.analysis.infrastructure.out.persistence;

import java.util.ArrayList;
import java.util.List;
import miasi.backend.database.JsonFileStorage;
import miasi.backend.domains.analysis.application.port.out.ISimulationDataProviderPort;
import miasi.backend.domains.analysis.application.port.out.ISimulationSessionRepositoryPort;
import miasi.backend.domains.analysis.domain._payload.PayloadOptimizationSession;
import miasi.backend.domains.analysis.domain._simulation.NominalSimulationSession;
import miasi.backend.domains.analysis.domain._simulation.ScenariosAnalysisSession;
import miasi.backend.domains.analysis.domain.schedule.Threat;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

@Repository
public class SimulationSessionJsonAdapter implements ISimulationSessionRepositoryPort,
    ISimulationDataProviderPort {

  private final JsonFileStorage<NominalSimulationSession> nominalDb = new JsonFileStorage<>(
      NominalSimulationSession.class);
  private final JsonFileStorage<ScenariosAnalysisSession> scenariosDb = new JsonFileStorage<>(
      ScenariosAnalysisSession.class);
  private final JsonFileStorage<PayloadOptimizationSession> payloadDb = new JsonFileStorage<>(
      PayloadOptimizationSession.class);

  private final String nominalFile;
  private final String scenariosFile;
  private final String payloadFile;

  private List<NominalSimulationSession> nominalSessions = new ArrayList<>();
  private List<ScenariosAnalysisSession> scenariosSessions = new ArrayList<>();

  public SimulationSessionJsonAdapter(
      @Value("${database.filename.analysis.nominal}") String nominalFile,
      @Value("${database.filename.analysis.scenarios}") String scenariosFile,
      @Value("${database.filename.analysis.payload}") String payloadFile
  ) {
    this.nominalFile = nominalFile;
    this.scenariosFile = scenariosFile;
    this.payloadFile = payloadFile;

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
    return List.of();
  }
}