package miasi.backend.analysis.infrastructure.out.persistence.memory;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import miasi.backend.analysis.application.model.MissionAnalysisResult;
import miasi.backend.analysis.application.port.out.AnalysisResultRepositoryPort;
import org.springframework.stereotype.Component;

@Component
public class InMemoryAnalysisResultRepository implements AnalysisResultRepositoryPort {
  private final ConcurrentMap<UUID, MissionAnalysisResult> results = new ConcurrentHashMap<>();

  @Override
  public void save(UUID manifestId, MissionAnalysisResult result) {
    results.put(manifestId, result);
  }

  @Override
  public Optional<MissionAnalysisResult> findByManifestId(UUID manifestId) {
    return Optional.ofNullable(results.get(manifestId));
  }
}
