package miasi.backend.domains.analysis.domain._simulation;

import java.util.List;
import lombok.Value;
import miasi.backend.domains.analysis.domain.core.DailyState;
import miasi.backend.domains.analysis.domain.core.VariantType;

@Value
public class SimulationVariant {

  VariantType type;                 // IDEAL lub REAL
  List<DailyState> timeline;        // Wygenerowana oś czasu (Sol po Solu)
  SimulationOutcome outcome;        // Pudełko zawierające status, deathSol i evacuationSol
}