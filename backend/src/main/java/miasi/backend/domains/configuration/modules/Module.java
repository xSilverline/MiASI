package miasi.backend.domains.configuration.modules;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import miasi.backend.domains.configuration.other.Resources;
import miasi.backend.enums.ModuleState;
import miasi.backend.enums.ResourceType;

import java.util.List;

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
  float weight;
  List<Resources> resourceConsumption;
  List<Resources> resourceProduction;

  public Module() {
    name = "default_laboratory";
    status = ModuleState.ACTIVE;
    category = ModuleCategory.UTILITY_MODULE;
    weight = 2137;
    resourceConsumption = List.of(new Resources[]{
        new Resources(
            ResourceType.ENERGY,
            1f
        ),
        new Resources(
            ResourceType.WATER,
            1f
        )
    });
    resourceProduction = List.of(new Resources[]{
        new Resources(
            ResourceType.FOOD,
            2.5f
        ),
        new Resources(
            ResourceType.OXYGEN,
            15
        )
    });
  }
}
