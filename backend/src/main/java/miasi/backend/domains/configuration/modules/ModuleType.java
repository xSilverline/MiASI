package miasi.backend.domains.configuration.modules;

import java.util.List;
import lombok.*;
import lombok.experimental.FieldDefaults;
import miasi.backend.domains.configuration.other.Resources;
import miasi.backend.sharedkernel.model.ResourceType;

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
