package miasi.backend.domains.analysis.domain.modules;

import java.util.List;
import lombok.Builder;
import lombok.Value;
import lombok.With;
import miasi.backend.domains.analysis.domain.core.Resource;

@Value
@Builder
public class Module {

  String id;
  String name;
  float weight;

  int minCount;

  @Builder.Default int maxCount = -1;

  List<Resource> production;
  List<Resource> consumption;

  @With ModuleState status;

  @With float efficiency;

  public Module copy() {
    List<Resource> productionCopy =
        this.production != null ? this.production.stream().map(Resource::copy).toList() : List.of();

    List<Resource> consumptionCopy =
        this.consumption != null
            ? this.consumption.stream().map(Resource::copy).toList()
            : List.of();

    return new Module(
        this.id,
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
