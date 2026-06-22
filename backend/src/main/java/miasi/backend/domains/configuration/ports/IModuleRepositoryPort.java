package miasi.backend.domains.configuration.ports;

import miasi.backend.domains.configuration.modules.Module;

import java.util.List;

public interface IModuleRepositoryPort {
  int add(Module module);

  List<Module> toJson();
}