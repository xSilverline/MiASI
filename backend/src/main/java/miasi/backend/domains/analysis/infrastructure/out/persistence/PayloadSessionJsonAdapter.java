package miasi.backend.domains.analysis.infrastructure.out.persistence;

import java.util.ArrayList;
import java.util.List;
import miasi.backend.database.JsonFileStorage;
import miasi.backend.domains.analysis.application.port.out.IPayloadSessionRepositoryPort;
import miasi.backend.domains.analysis.domain._payload.PayloadOptimizationSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

@Repository
public class PayloadSessionJsonAdapter implements IPayloadSessionRepositoryPort {

  private final JsonFileStorage<PayloadOptimizationSession> database = new JsonFileStorage<>(
      PayloadOptimizationSession.class);
  private final String filePath;
  private List<PayloadOptimizationSession> sessions = new ArrayList<>();

  public PayloadSessionJsonAdapter(
      @Value("${database.filename.analysis.payload}") String filePath
  ) {
    this.filePath = filePath;
    List<PayloadOptimizationSession> loaded = database.loadListFromFile(filePath);
    if (loaded != null) {
      this.sessions = new ArrayList<>(loaded);
    }
  }

  @Override
  public void save(PayloadOptimizationSession session) {
    sessions.removeIf(s -> s.getId().equals(session.getId()));
    sessions.add(session);

    database.saveListToFile(sessions, filePath);
    System.out.println("Zapisano sesję payload do: " + filePath);
  }
}