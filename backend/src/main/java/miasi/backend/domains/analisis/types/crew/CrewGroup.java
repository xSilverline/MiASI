package miasi.backend.domains.analisis.types.crew;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import miasi.backend.domains.analisis.types.crew.ConsumptionProfile;
import miasi.backend.domains.analisis.types.core.Resource;

import java.util.List;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
public class CrewGroup {
    String name;
    int count;
    ConsumptionProfile minimalNeeds;
    ConsumptionProfile optimalNeeds;

    public List<Resource> getDailyDemand(ConsumptionMode mode) {
        // Zwraca potrzeby profilu na podstawie trybu
        return mode == ConsumptionMode.OPTIMAL ?
                optimalNeeds.getDailyConsumption() :
                minimalNeeds.getDailyConsumption();
    }
}