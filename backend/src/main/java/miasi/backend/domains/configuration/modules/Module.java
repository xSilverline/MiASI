package miasi.backend.domains.configuration.modules;

import lombok.*;
import lombok.experimental.FieldDefaults;
import miasi.backend.sharedkernel.model.ModuleState;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@EqualsAndHashCode
@Builder(toBuilder = true)
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
