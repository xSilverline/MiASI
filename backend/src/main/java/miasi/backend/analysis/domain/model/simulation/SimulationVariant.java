package miasi.backend.analysis.domain.model.simulation;

import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import miasi.backend.analysis.domain.model.core.DailyState;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SimulationVariant {
  VariantType type;
  Status status;
  List<DailyState> timeline;
  Integer deathSol;
  Integer evacuationSol;

  public SimulationVariant(VariantType type, Status status, List<DailyState> timeline) {
    this.type = type;
    this.status = status;
    this.timeline = timeline;
  }
}
