package miasi.backend.domains.analysis.domain.modules;

import java.util.List;
import lombok.Value;
import lombok.With;
import miasi.backend.domains.analysis.domain.core.DailyBalance;
import miasi.backend.domains.analysis.domain.core.Resource;

@Value
public class Module {

  String id;
  String name;
  float weight;
  int minCount;       // Założenie: 0 oznacza brak wymagań minimum
  Integer maxCount;   // Może być null dla braku limitu (np. panele słoneczne)

  List<Resource> production;
  List<Resource> consumption;

  @With // Magia Lomboka! Sam stworzy metodę withStatus(ModuleState)
  ModuleState status;

  @With // Sam stworzy metodę withEfficiency(float) na wypadek awarii
  float efficiency;

  public DailyBalance getDailyBalance() {
    // W przyszłości: przeliczenie production i consumption przez wartość efficiency (np. 0.8)
    return new DailyBalance(production, consumption);
  }

  // Twoja metoda deep copy przydaje się na start symulacji (żeby klonować oryginalny sprzęt z Ziemi).
  // W środku symulacji (dzień po dniu) symulatory będą używać już tylko withStatus()!
  public Module copy() {
    List<Resource> productionCopy = this.production != null ?
        this.production.stream().map(Resource::copy).toList() : List.of();

    List<Resource> consumptionCopy = this.consumption != null ?
        this.consumption.stream().map(Resource::copy).toList() : List.of();

    return new Module(
        this.id,
        this.name,
        this.weight,
        this.minCount,
        this.maxCount,
        productionCopy,
        consumptionCopy,
        this.status,
        this.efficiency
    );
  }
}