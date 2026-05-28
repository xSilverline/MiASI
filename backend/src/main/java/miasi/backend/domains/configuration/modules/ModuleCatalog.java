package miasi.backend.domains.configuration.modules;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public final class ModuleCatalog {
  List<ModuleType> moduleTypes;
  List<Module> modules;
  private static final ModuleCatalog INSTANCE = new ModuleCatalog();

  private ModuleCatalog() {
    moduleTypes = new ArrayList<>();
    modules = new ArrayList<>();
  }

  public static ModuleCatalog getInstance() {
    return INSTANCE;
  }

  public void add(Module module) {
    modules.add(module);
  }

  public void add(ModuleType type) {
    moduleTypes.add(type);
  }

  public void remove(Module module) {
    modules.remove(module);
  }

  public void remove(ModuleType moduleType) {
    moduleTypes.remove(moduleType);
  }
}
