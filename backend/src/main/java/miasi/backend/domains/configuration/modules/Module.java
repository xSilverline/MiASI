package miasi.backend.domains.configuration.modules;

import javafx.util.Pair;
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
  Pair<Float, Float> resourcesAmount;
  ModuleState status;
  ModuleType type;
}
