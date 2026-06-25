package miasi.backend.domains.analysis.domain._simulation;

import lombok.Value;

@Value
public class NominalSimulationCompletedEvent {
  NominalSimulationSession session;
}
