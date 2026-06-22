package miasi.backend.analysis.domain.service;

import java.util.List;
import miasi.backend.analysis.domain.model.core.Resource;
import miasi.backend.analysis.domain.model.modules.Module;
import miasi.backend.analysis.domain.model.schedule.Threat;
import miasi.backend.common.domain.model.ModuleState;

public class ThreatProcessor {

  public void process(
      int currentSol, List<Threat> threats, List<Module> currentModules, List<Resource> warehouse) {
    // find active failures in currentSol and apply their effects to modules (e.g. status change) or
    // storage (e.g. leak)
    if (threats == null) return;

    for (Threat threat : threats) {

      // check if the threat is active on the current day
      if (currentSol >= threat.getSol()
          && currentSol < threat.getSol() + threat.getDurationSols()) {

        // apply effects based on ImpactType
        switch (threat.getType().name()) {
          case "QUANTITY_CHANGE":
            if (warehouse != null) {
              for (Resource res : warehouse) {
                if (res.getType().name().equalsIgnoreCase(threat.getTargetIdentifier())) {
                  float newAmount = res.getAmount() - threat.getImpactValue();
                  // the amount cannot drop below zero only due to a leak
                  res.setAmount(Math.max(0.0f, newAmount));
                  break;
                }
              }
            }
            break;

          case "EFFICIENCY_CHANGE":
            if (currentModules != null) {
              for (Module module : currentModules) {
                if (module.getName().equalsIgnoreCase(threat.getTargetIdentifier())) {
                  float newEfficiency = module.getEfficiency() - threat.getImpactValue();
                  // efficiency cannot be negative
                  module.setEfficiency(Math.max(0.0f, newEfficiency));
                }
              }
            }
            break;

          case "STATE_CHANGE":
            if (currentModules != null) {
              for (Module module : currentModules) {
                if (module.getName().equalsIgnoreCase(threat.getTargetIdentifier())) {
                  // status change to destroyed
                  module.setStatus(ModuleState.DESTROYED);
                  module.setEfficiency(0.0f);
                }
              }
            }
            break;
        }
      }
    }
  }
}
