package miasi.backend.domains.analysis.domain.core;

import java.util.List;
import java.util.Set;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import miasi.backend.domains.analysis.domain.crew.ConsumptionMode;
import miasi.backend.domains.analysis.domain.modules.Module;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
public class DailyState {

  int sol;
  List<Resource> warehouse;
  DailyBalance balance;
  ConsumptionMode mode;
  List<Module> modules;
  Set<ObservationType> observations; // !!!
}
