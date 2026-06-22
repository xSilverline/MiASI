package miasi.backend.analysis.application.port.in;

import miasi.backend.analysis.application.MissionAnalysisResult;
import miasi.backend.domains.analysis.types.input.MissionManifest;

public interface RunMissionAnalysisPort {
  MissionAnalysisResult run(MissionManifest manifest);
}
