package miasi.backend.configuration.application.port.out;

import miasi.backend.configuration.domain.model.Module;
import miasi.backend.configuration.domain.model.ModuleCatalog;
import miasi.backend.configuration.domain.model.ModuleType;

public interface ModuleRepositoryPort {
  int add(Module module);

  int add(ModuleType type);

  ModuleCatalog getCatalog();
}
