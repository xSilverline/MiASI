package miasi.backend.domains.configuration.modules;

import lombok.*;
import lombok.experimental.FieldDefaults;
import miasi.backend.domains.configuration.other.Resources;
import miasi.backend.enums.ResourceType;

import java.util.List;

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

  static public ModuleType genSample() {
    return new ModuleType(
        "laboratory",
        List.of(new Resources[]{
            new Resources(
                ResourceType.ENERGY,
                1f
            ),
            new Resources(
                ResourceType.WATER,
                1f
            )
        }),
        List.of(new Resources[]{
            new Resources(
                ResourceType.FOOD,
                2.5f
            ),
            new Resources(
                ResourceType.OXYGEN,
                15
            )
        }));
  }
}
