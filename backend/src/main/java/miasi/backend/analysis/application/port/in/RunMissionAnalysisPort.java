package miasi.backend.analysis.application.port.in;

import miasi.backend.analysis.application.model.MissionAnalysisResult;
import miasi.backend.analysis.domain.model.input.MissionManifest;

public interface RunMissionAnalysisPort {
  MissionAnalysisResult run(MissionManifest manifest);
}
