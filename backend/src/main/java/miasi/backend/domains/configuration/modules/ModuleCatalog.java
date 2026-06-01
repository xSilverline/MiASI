package miasi.backend.domains.configuration.modules;

import lombok.AccessLevel;
import lombok.Synchronized;
import lombok.experimental.FieldDefaults;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;

import java.util.ArrayList;
import java.util.List;

@Component
@Scope(WebApplicationContext.SCOPE_APPLICATION)
@FieldDefaults(level = AccessLevel.PRIVATE)
public final class ModuleCatalog {
  final List<ModuleType> moduleTypes = new ArrayList<>();
  final List<Module> modules = new ArrayList<>();

  public ModuleCatalog() {
    //TODO: zczytywanie z bazy
  }

  @Synchronized
  public int add(Module module) {
    modules.add(module);
    return modules.size() - 1;
  }

  @Synchronized
  public int add(ModuleType type) {
    moduleTypes.add(type);
    return moduleTypes.size() - 1;
  }

  @Synchronized
  public void remove(Module module) {
    modules.remove(module);
  }

  @Synchronized
  public void remove(ModuleType moduleType) {
    moduleTypes.remove(moduleType);
  }

  @Synchronized
  public List<Module> getModules() {
    return List.copyOf(modules);
  }

  @Synchronized
  public List<ModuleType> getModuleTypes() {
    return List.copyOf(moduleTypes);
  }
}
