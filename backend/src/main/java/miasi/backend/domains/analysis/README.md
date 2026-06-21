# Moje notatki

Trzeba będzie dodać jeszcze jakiś przelicznik wagi gdzieś. Może to? A może enum? Nie jestem pewna,
zostawiam w takim razie wam decyzję, żeby nie utrudnić przypadkowo

```java
package miasi.backend.config;

import miasi.backend.enums.ResourceType;
import miasi.backend.domains.analysis.types.core.ResourceWeightDictionary;
import java.util.Map;

public class MissionConfiguration {

    // Metoda, która buduje i zwraca konfigurację wag
    public ResourceWeightDictionary createWeightDictionary() {
        Map<ResourceType, Float> config = Map.of(
                ResourceType.OXYGEN, 1.2f,
                ResourceType.WATER, 1.1f,
                ResourceType.CALORIES, 0.5f,        // Podmienione FOOD na CALORIES
                ResourceType.CARBON_DIOXIDE, 1.0f,  // Dodane do mapy, skoro doszło do enuma
                ResourceType.ENERGY, 0.0f
        );
        
        return new ResourceWeightDictionary(config);
    }
}```