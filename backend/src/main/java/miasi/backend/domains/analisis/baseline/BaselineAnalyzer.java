package miasi.backend.domains.analisis.baseline;

import lombok.RequiredArgsConstructor;
import miasi.backend.domains.analisis.services.PayloadOptimizer;
import miasi.backend.domains.analisis.types.input.MissionManifest;

@RequiredArgsConstructor
public class BaselineAnalyzer {

    private final PayloadOptimizer payloadOptimizer;

    public BaselineAnalysisSession analyze(MissionManifest manifest) {
        // zleć PayloadOptimizerowi znalezienie najlżejszej konfiguracji i zapakuj wynik w BaselineAnalysisSession
        return null;
    }
}