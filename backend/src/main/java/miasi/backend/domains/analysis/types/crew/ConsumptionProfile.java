package miasi.backend.domains.analysis.types.crew;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import miasi.backend.domains.analysis.types.core.Resource;

import java.util.List;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
public class ConsumptionProfile {
  List<Resource> dailyConsumption; // lista zasobów określająca zapotrzebowanie (np. profil minimalny lub optymalny)
}