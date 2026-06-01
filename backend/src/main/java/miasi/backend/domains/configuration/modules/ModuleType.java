package miasi.backend.domains.configuration.modules;

import lombok.*;
import lombok.experimental.FieldDefaults;
import miasi.backend.domains.configuration.other.Resources;

import java.util.List;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor
public class ModuleType {
  String name;
  List<Resources> resourceConsumption;
  List<Resources> resourceProduction;
}
