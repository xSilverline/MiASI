package miasi.backend.domains.analisis.simulation;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import miasi.backend.domains.analisis.types.core.DailyState;
import miasi.backend.enums.Status;
import miasi.backend.enums.VariantType;

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