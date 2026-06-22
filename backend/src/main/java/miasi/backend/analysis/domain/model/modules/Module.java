package miasi.backend.analysis.domain.model.modules;

import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import miasi.backend.analysis.domain.model.core.DailyBalance;
import miasi.backend.analysis.domain.model.core.Resource;
import miasi.backend.common.domain.model.ModuleState;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
public class Module {
  String name;
  float weight;
  int minCount; // Może być null jeśli wcale nie musi to być
  Integer maxCount; // Może być null dla braku limitu (np. panele słoneczne)
  List<Resource> production;
  List<Resource> consumption;
  ModuleState status;
  float efficiency;

  public DailyBalance getDailyBalance() {
    // W przyszłości: przeliczenie production i consumption przez wartość efficiency (np. 0.8)
    return new DailyBalance(production, consumption);
  }

  public Module copy() {
    // deepcopy
    List<Resource> productionCopy =
        this.production != null
            ? this.production.stream().map(Resource::copy).toList()
            : new java.util.ArrayList<>();

    List<Resource> consumptionCopy =
        this.consumption != null
            ? this.consumption.stream().map(Resource::copy).toList()
            : new java.util.ArrayList<>();

    return new Module(
        this.name,
        this.weight,
        this.minCount,
        this.maxCount,
        productionCopy,
        consumptionCopy,
        this.status,
        this.efficiency);
  }
}
