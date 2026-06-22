package miasi.backend.domains.analysis.types.result;

import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import miasi.backend.domains.analysis.types.core.Resource;
import miasi.backend.domains.analysis.types.modules.Module;

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
