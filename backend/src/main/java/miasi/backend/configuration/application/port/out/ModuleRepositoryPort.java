package miasi.backend.configuration.application.port.out;

import miasi.backend.configuration.domain.model.Module;

import java.util.List;

public interface ModuleRepositoryPort {
  int add(Module module);

  List<Module> getCatalog();
}
