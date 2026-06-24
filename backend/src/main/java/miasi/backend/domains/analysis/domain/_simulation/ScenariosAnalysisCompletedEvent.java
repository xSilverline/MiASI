package miasi.backend.domains.analysis.domain._simulation;

import lombok.Value;
import miasi.backend.domains.analysis.domain._simulation.ScenariosAnalysisSession;

@Value
public class ScenariosAnalysisCompletedEvent {
  ScenariosAnalysisSession session;
}