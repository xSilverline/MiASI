package miasi.backend.analysis.domain.model.result;

import lombok.Value;
import miasi.backend.analysis.domain.model.simulation.Status;

@Value
public class SimulationOutcome {
  Status status;
  Integer deathSol;
  Integer evacuationSol;
}
