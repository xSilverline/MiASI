package miasi.backend.configuration.domain.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import miasi.backend.common.domain.model.ModuleState;
import miasi.backend.common.domain.model.ResourceType;

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
