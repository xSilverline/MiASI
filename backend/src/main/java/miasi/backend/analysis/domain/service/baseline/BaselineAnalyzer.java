package miasi.backend.analysis.domain.service.baseline;

import java.util.ArrayList;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import miasi.backend.analysis.domain.model.baseline.BaselineAnalysisSession;
import miasi.backend.analysis.domain.model.input.MissionManifest;
import miasi.backend.analysis.domain.model.result.OptimalConfiguration;
import miasi.backend.analysis.domain.service.PayloadOptimizer;
import miasi.backend.analysis.domain.service.TimelineSimulator;

@RequiredArgsConstructor
public class BaselineAnalyzer {

  private final PayloadOptimizer payloadOptimizer;
  private final TimelineSimulator timelineSimulator;

  public BaselineAnalysisSession analyze(MissionManifest manifest) {
    if (manifest == null) {
      throw new IllegalArgumentException("Mission manifest is required");
    }

    OptimalConfiguration configuration = payloadOptimizer.findOptimalConfiguration(manifest);
    return new BaselineAnalysisSession(
        UUID.randomUUID(),
        "COMPLETED",
        timelineSimulator.simulate(
            manifest,
            new ArrayList<>(configuration.getOptimalModules()),
            new ArrayList<>(configuration.getStartingResources())),
        configuration);
  }
}
