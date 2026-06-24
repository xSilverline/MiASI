package miasi.backend.domains.analysis.domain._simulation;

import lombok.Value;
import miasi.backend.domains.analysis.domain.core.Status;

@Value
public class SimulationOutcome {

  Status status;
  Integer deathSol;
  Integer evacuationSol;
}