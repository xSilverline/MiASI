package miasi.backend.domains.configuration.ports;

import java.util.List;
import miasi.backend.domains.configuration.modules.Module;

public interface IModuleRepositoryPort {
  int add(Module module);

  List<Module> toJson();
}
