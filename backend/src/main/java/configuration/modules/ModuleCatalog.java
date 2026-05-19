package configuration.modules;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
public class ModuleCatalog {
  List<Module> modules;

  public void add(Module module) {
    modules.add(module);
  }

  public String getFirstModuleName() {
    return modules.getFirst().getName();
  }

}
