package miasi.backend.domains.analysis.domain._simulation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import miasi.backend.domains.analysis.domain.core.Status;

@Value
@Builder
@AllArgsConstructor
public class SimulationOutcome {

  Status status;
  Integer deathSol;
  Integer evacuationSol;
}