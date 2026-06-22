package miasi.backend.configuration.application.port.in;

import miasi.backend.configuration.domain.model.Module;
import miasi.backend.configuration.domain.model.ModuleType;

public interface ManageModuleCatalogUseCase {
  int addModule(Module module);

  int addModuleType(ModuleType type);
}
