package miasi.backend.configuration.domain.model;

import lombok.*;
import lombok.experimental.FieldDefaults;
import miasi.backend.common.domain.model.ModuleState;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@EqualsAndHashCode
@Builder(toBuilder = true)
public class Module {
  String name;
  ModuleState status;
  ModuleCategory category;
  ModuleType type;
  float weight;

  public Module() {
    name = "default_laboratory";
    status = ModuleState.ACTIVE;
    category = ModuleCategory.SCIENCE_MODULE;
    type = ModuleType.genSample();
    weight = 2137;
  }

  public Module(String name, ModuleState status, ModuleType type, float weight) {
    this(name, status, ModuleCategory.UTILITY_MODULE, type, weight);
  }
}
