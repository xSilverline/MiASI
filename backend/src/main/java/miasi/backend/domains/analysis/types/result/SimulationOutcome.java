package miasi.backend.domains.analysis.types.result;

import lombok.Value;
import miasi.backend.domains.analysis.simulation.Status;

@Value
public class SimulationOutcome {
  Status status;
  Integer deathSol;
  Integer evacuationSol;
}
