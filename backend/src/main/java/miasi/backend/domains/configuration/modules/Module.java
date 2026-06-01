package miasi.backend.domains.configuration.modules;

import lombok.*;
import lombok.experimental.FieldDefaults;
import miasi.backend.enums.ModuleState;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor
public class Module {
  String name;
  ModuleState status;
  ModuleType type;
  float weight;
}
