package miasi.backend.domains.analysis.domain.schedule;

import java.util.List;
import miasi.backend.domains.analysis.domain.core.Resource;
import miasi.backend.domains.analysis.domain.modules.Module;
import miasi.backend.domains.analysis.domain.modules.ModuleState;

public class ThreatProcessor {

  public void process(
      int currentSol, List<Threat> threats, List<Module> currentModules, List<Resource> warehouse) {

    if (threats == null) {
      return;
    }

    for (Threat threat : threats) {
      if (currentSol >= threat.getSol()
          && currentSol < threat.getSol() + threat.getDurationSols()) {

        switch (threat.getType().name()) {
          case "QUANTITY_CHANGE":
            if (warehouse != null) {
              warehouse.replaceAll(
                  res -> {
                    if (res.getType().name().equalsIgnoreCase(threat.getTargetIdentifier())) {
                      float newAmount = res.getAmount() - threat.getImpactValue();
                      return res.withAmount(Math.max(0.0f, newAmount));
                    }
                    return res;
                  });
            }
            break;

          case "EFFICIENCY_CHANGE":
            if (currentModules != null) {
              currentModules.replaceAll(
                  module -> {
                    if (module.getName().equalsIgnoreCase(threat.getTargetIdentifier())) {
                      float newEfficiency = module.getEfficiency() - threat.getImpactValue();
                      return module.withEfficiency(Math.max(0.0f, newEfficiency));
                    }
                    return module;
                  });
            }
            break;

          case "STATE_CHANGE":
            if (currentModules != null) {
              currentModules.replaceAll(
                  module -> {
                    if (module.getName().equalsIgnoreCase(threat.getTargetIdentifier())) {
                      return module.withStatus(ModuleState.DESTROYED).withEfficiency(0.0f);
                    }
                    return module;
                  });
            }
            break;
        }
      }
    }
  }
}
