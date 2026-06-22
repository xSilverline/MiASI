package miasi.backend.analysis.domain.service;

import java.util.List;
import miasi.backend.analysis.domain.model.core.Resource;
import miasi.backend.analysis.domain.model.modules.Module;
import miasi.backend.analysis.domain.model.schedule.Delivery;

public class DeliveryProcessor {

  public void process(
      int currentSol,
      List<Delivery> deliveries,
      List<Module> currentModules,
      List<Resource> warehouse) {
    // if there is a delivery due today: add its modules to currentModules and its stock to
    // warehouse
    if (deliveries == null) return;
    for (Delivery delivery : deliveries) {

      // only today (sol)
      if (delivery.getSol() == currentSol) {

        // add modules to the base
        if (delivery.getModules() != null && currentModules != null) {
          currentModules.addAll(delivery.getModules());
        }

        // add resources to the warehouse
        if (delivery.getResources() != null && warehouse != null) {

          for (Resource deliveredResource : delivery.getResources()) {
            boolean resourceExists = false;

            for (Resource existingResource : warehouse) {
              if (existingResource.getType() == deliveredResource.getType()) {
                existingResource.setAmount(
                    existingResource.getAmount() + deliveredResource.getAmount());
                resourceExists = true;
                break;
              }
            }

            // if the base didn't have this resource before
            if (!resourceExists) {
              warehouse.add(
                  new Resource(deliveredResource.getType(), deliveredResource.getAmount()));
            }
          }
        }
      }
    }
  }
}
