package miasi.backend.domains.analysis.infrastructure.out.persistence;

import tools.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import miasi.backend.domains.analysis.application.port.out.IPayloadSessionRepositoryPort;
import miasi.backend.domains.analysis.domain._payload.PayloadOptimizationSession;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PayloadSessionJsonAdapter implements IPayloadSessionRepositoryPort {

  private final ObjectMapper objectMapper;

  private static final String FILE_PATH = "payload_sessions_database.json";

  @Override
  public void save(PayloadOptimizationSession session) {
    File file = new File(FILE_PATH);
    objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, session);
    System.out.println("Zapisano sesję do pliku: " + file.getAbsolutePath());
  }

}