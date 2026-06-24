package miasi.backend.domains.analysis.domain._payload;

import java.util.List;
import lombok.Value;
import miasi.backend.domains.analysis.domain.core.Resource;
import miasi.backend.domains.analysis.domain.modules.Module;

@Value
public class OptimalConfiguration {

  List<Module> optimalModules;
  List<Resource> startingResources;
  float totalWeight;
  Boolean isWeightLimitExceeded;

}