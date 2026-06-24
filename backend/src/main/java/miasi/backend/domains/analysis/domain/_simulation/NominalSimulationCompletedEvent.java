package miasi.backend.domains.analysis.domain._simulation;

import lombok.Value;
import miasi.backend.domains.analysis.domain._simulation.NominalSimulationSession;

@Value
public class NominalSimulationCompletedEvent {
  NominalSimulationSession session;
}