package miasi.backend.domains.analysis.domain.crew;

import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import miasi.backend.domains.analysis.domain.core.Resource;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
public class ConsumptionProfile {

  List<Resource> dailyConsumption;
}