package miasi.backend.analysis.application.port.out;

import java.util.Optional;
import java.util.UUID;
import miasi.backend.analysis.application.model.MissionAnalysisResult;

public interface AnalysisResultRepositoryPort {
  void save(UUID manifestId, MissionAnalysisResult result);

  Optional<MissionAnalysisResult> findByManifestId(UUID manifestId);
}
