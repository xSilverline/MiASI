package miasi.backend.configuration.application.port.in;

import miasi.backend.domains.configuration.modules.Module;
import miasi.backend.domains.configuration.modules.ModuleType;

public interface ManageModuleCatalogUseCase {
  int addModule(Module module);

  int addModuleType(ModuleType type);
}
