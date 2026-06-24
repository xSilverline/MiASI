package miasi.backend.domains.analysis.domain.schedule;

import java.util.List;
import miasi.backend.domains.analysis.domain.core.Resource;
import miasi.backend.domains.analysis.domain.modules.Module;

public class DeliveryProcessor {

  public void process(int currentSol, List<Delivery> deliveries, List<Module> currentModules,
      List<Resource> warehouse) {
    if (deliveries == null) {
      return;
    }

    for (Delivery delivery : deliveries) {

      if (delivery.getSol() == currentSol) {

        if (delivery.getModules() != null && currentModules != null) {
          currentModules.addAll(delivery.getModules());
        }

        if (delivery.getResources() != null && warehouse != null) {

          for (Resource deliveredResource : delivery.getResources()) {
            boolean resourceExists = false;

            for (int i = 0; i < warehouse.size(); i++) {
              Resource existingResource = warehouse.get(i);

              if (existingResource.getType() == deliveredResource.getType()) {
                warehouse.set(i, existingResource.withAmount(
                    existingResource.getAmount() + deliveredResource.getAmount()
                ));
                resourceExists = true;
                break;
              }
            }

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