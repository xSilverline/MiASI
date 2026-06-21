package miasi.backend.domains.analysis.baseline;

import lombok.RequiredArgsConstructor;
import miasi.backend.domains.analysis.services.PayloadOptimizer;
import miasi.backend.domains.analysis.types.input.MissionManifest;

@RequiredArgsConstructor
public class BaselineAnalyzer {

  private final PayloadOptimizer payloadOptimizer;

  public BaselineAnalysisSession analyze(MissionManifest manifest) {
    // zleć PayloadOptimizerowi znalezienie najlżejszej konfiguracji i zapakuj wynik w BaselineAnalysisSession
    return null;
  }
}