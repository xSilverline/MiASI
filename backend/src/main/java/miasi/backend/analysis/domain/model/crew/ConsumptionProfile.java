package miasi.backend.analysis.domain.model.crew;

import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import miasi.backend.analysis.domain.model.core.Resource;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
public class ConsumptionProfile {
  List<Resource>
      dailyConsumption; // lista zasobów określająca zapotrzebowanie (np. profil minimalny lub
  // optymalny)
}
