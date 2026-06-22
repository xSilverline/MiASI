package miasi.backend.configuration.domain.model;

import java.util.List;
import lombok.*;
import lombok.experimental.FieldDefaults;
import miasi.backend.common.domain.model.ResourceType;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class ModuleType {
  String name;
  List<Resources> resourceConsumption;
  List<Resources> resourceProduction;

  public static ModuleType genSample() {
    return new ModuleType(
        "laboratory",
        List.of(
            new Resources[] {
              new Resources(ResourceType.ENERGY, 1f), new Resources(ResourceType.WATER, 1f)
            }),
        List.of(
            new Resources[] {
              new Resources(ResourceType.FOOD, 2.5f), new Resources(ResourceType.OXYGEN, 15)
            }));
  }
}
