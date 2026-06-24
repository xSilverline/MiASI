package miasi.backend.domains.analysis.domain._simulation;

import lombok.Value;

@Value
public class ScenariosAnalysisCompletedEvent {
  ScenariosAnalysisSession session;
}
