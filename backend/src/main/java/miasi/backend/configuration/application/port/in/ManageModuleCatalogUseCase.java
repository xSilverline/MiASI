package miasi.backend.configuration.application.port.in;

import miasi.backend.configuration.domain.model.Module;

public interface ManageModuleCatalogUseCase {
  int addModule(Module module);
}
