package miasi.backend.domains.configuration.modules;

import java.util.List;

public record ModuleCatalog(
    List<Module> moduleList,
    List<ModuleType> typeList
) {
}
