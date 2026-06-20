package miasi.backend.domains.analisis.types.result;

import lombok.Value;
import miasi.backend.enums.Status;

@Value
public class SimulationOutcome {
  Status status;
  Integer deathSol;
  Integer evacuationSol;
}