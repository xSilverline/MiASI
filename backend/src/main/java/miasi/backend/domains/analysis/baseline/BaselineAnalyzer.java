package miasi.backend.domains.analysis.baseline;

import java.util.ArrayList;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import miasi.backend.domains.analysis.services.PayloadOptimizer;
import miasi.backend.domains.analysis.services.TimelineSimulator;
import miasi.backend.domains.analysis.types.input.MissionManifest;
import miasi.backend.domains.analysis.types.result.OptimalConfiguration;

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
