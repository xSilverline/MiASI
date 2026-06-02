package miasi.backend.domains.configuration.modules;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import miasi.backend.enums.ModuleState;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
public class Module {
  String name;
  ModuleState status;
  ModuleType type;
  float weight;

  public Module() {
    name = "default_laboratory";
    status = ModuleState.ACTIVE;
    type = ModuleType.genSample();
    weight = 2137;
  }
}
