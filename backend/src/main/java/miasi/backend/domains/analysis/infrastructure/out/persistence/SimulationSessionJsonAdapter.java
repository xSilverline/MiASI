package miasi.backend.domains.analysis.infrastructure.out.persistence;

import java.io.File;
import java.util.List;
import lombok.RequiredArgsConstructor;
import miasi.backend.domains.analysis.application.port.out.ISimulationDataProviderPort;
import miasi.backend.domains.analysis.application.port.out.ISimulationSessionRepositoryPort;
import miasi.backend.domains.analysis.domain._payload.PayloadOptimizationSession;
import miasi.backend.domains.analysis.domain._simulation.NominalSimulationSession;
import miasi.backend.domains.analysis.domain._simulation.ScenariosAnalysisSession;
import miasi.backend.domains.analysis.domain.schedule.Threat;
import org.springframework.stereotype.Repository;
import tools.jackson.databind.ObjectMapper;

@Repository
@RequiredArgsConstructor
public class SimulationSessionJsonAdapter implements ISimulationSessionRepositoryPort,
    ISimulationDataProviderPort {

  private final ObjectMapper objectMapper;
  private static final String NOMINAL_DIR = "data/sessions/nominal/";
  private static final String SCENARIOS_DIR = "data/sessions/scenarios/";
  private static final String PAYLOAD_DIR = "data/sessions/payload/";

  @Override
  public void saveNominal(NominalSimulationSession session) {
    saveToJson(NOMINAL_DIR, session.getId(), session);
  }

  @Override
  public void saveScenarios(ScenariosAnalysisSession session) {
    saveToJson(SCENARIOS_DIR, session.getId(), session);
  }

  @Override
  public PayloadOptimizationSession getPayloadSession(String payloadSessionId) {
    return readFromJson(PAYLOAD_DIR, payloadSessionId, PayloadOptimizationSession.class);
  }

  @Override
  public NominalSimulationSession getNominalSession(String nominalSessionId) {
    return readFromJson(NOMINAL_DIR, nominalSessionId, NominalSimulationSession.class);
  }

  @Override
  public List<Threat> getThreatsForSchedule(String scheduleId) {
    return List.of();
  }

  private void saveToJson(String dirPath, String id, Object data) {
    File dir = new File(dirPath);
    if (!dir.exists()) {
      dir.mkdirs();
    }
    objectMapper.writerWithDefaultPrettyPrinter()
        .writeValue(new File(dirPath + id + ".json"), data);
  }

  private <T> T readFromJson(String dirPath, String id, Class<T> clazz) {
    return objectMapper.readValue(new File(dirPath + id + ".json"), clazz);
  }
}