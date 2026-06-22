package miasi.backend.analysis.domain.model.result;

import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import miasi.backend.analysis.domain.model.core.Resource;
import miasi.backend.analysis.domain.model.modules.Module;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
public class OptimalConfiguration {
  List<Module> optimalModules; // lista_modulow: optymalna konfiguracja bazy na sol 0
  List<Resource> startingResources; // zapasy_sol_0: wyliczone bezpieczne zapasy startowe
  float totalWeight; // całkowita waga startowa (moduły + zapasy)
  boolean isWeightLimitExceeded; // ostrzezenie_o_wadze: true jeśli totalWeight > maxWeightSolZero
}
