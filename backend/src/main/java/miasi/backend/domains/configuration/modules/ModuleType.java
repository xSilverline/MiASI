package miasi.backend.domains.configuration.modules;

import lombok.*;
import lombok.experimental.FieldDefaults;
import miasi.backend.enums.ResourceType;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor
public class ModuleType {
  String name;
  ResourceType resourceConsumption;
  ResourceType resourceProduction;
}
