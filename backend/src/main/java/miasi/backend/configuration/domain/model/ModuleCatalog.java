package miasi.backend.configuration.domain.model;

import java.util.List;

public record ModuleCatalog(List<Module> moduleList, List<ModuleType> typeList) {}
