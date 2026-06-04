package miasi.backend.domains.configuration.ports;

import miasi.backend.domains.configuration.modules.Module;
import miasi.backend.domains.configuration.modules.ModuleCatalog;
import miasi.backend.domains.configuration.modules.ModuleType;

public interface IModuleRepositoryPort {
    int add(Module module);
    int add(ModuleType type);
    ModuleCatalog toJson();
}