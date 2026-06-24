package miasi.backend.domains.analysis.domain.schedule;

import java.util.List;
import miasi.backend.domains.analysis.domain.core.Resource;
import miasi.backend.domains.analysis.domain.modules.Module;
import miasi.backend.domains.analysis.domain.modules.ModuleState;

public class ThreatProcessor {

  public void process(int currentSol, List<Threat> threats, List<Module> currentModules,
      List<Resource> warehouse) {

    // find active failures in currentSol and apply their effects
    if (threats == null) {
      return;
    }

    for (Threat threat : threats) {
      // check if the threat is active on the current day
      if (currentSol >= threat.getSol()
          && currentSol < threat.getSol() + threat.getDurationSols()) {

        // apply effects based on ImpactType
        switch (threat.getType().name()) {

          case "QUANTITY_CHANGE":
            if (warehouse != null) {
              // FUNKCYJNA ZMIANA: Podmieniamy zasób w liście na nową kopię z nową wartością
              warehouse.replaceAll(res -> {
                if (res.getType().name().equalsIgnoreCase(threat.getTargetIdentifier())) {
                  float newAmount = res.getAmount() - threat.getImpactValue();
                  // the amount cannot drop below zero only due to a leak
                  return res.withAmount(Math.max(0.0f, newAmount));
                }
                return res; // Jeśli to nie ten zasób, zostawiamy go w spokoju
              });
            }
            break;

          case "EFFICIENCY_CHANGE":
            if (currentModules != null) {
              currentModules.replaceAll(module -> {
                if (module.getName().equalsIgnoreCase(threat.getTargetIdentifier())) {
                  float newEfficiency = module.getEfficiency() - threat.getImpactValue();
                  // efficiency cannot be negative
                  return module.withEfficiency(Math.max(0.0f, newEfficiency));
                }
                return module;
              });
            }
            break;

          case "STATE_CHANGE":
            if (currentModules != null) {
              currentModules.replaceAll(module -> {
                if (module.getName().equalsIgnoreCase(threat.getTargetIdentifier())) {
                  // status change to destroyed AND efficiency to 0
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