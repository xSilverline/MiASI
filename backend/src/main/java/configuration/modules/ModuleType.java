package configuration.modules;

import configuration.enums.ResourceType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
public class ModuleType {
  String name;
  ResourceType resourceConsumption;
  ResourceType resourceProduction;
}
