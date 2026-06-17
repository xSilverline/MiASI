package miasi.backend.domains.analysis.simulation;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import miasi.backend.domains.analysis.types.core.DailyState;

import java.util.List;

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