package miasi.backend.domains.analisis.services;

import lombok.RequiredArgsConstructor;
import miasi.backend.domains.analisis.types.core.Resource;
import miasi.backend.domains.analisis.types.modules.Module;

import java.util.List;

@RequiredArgsConstructor
public class WeightCalculator {

    // private final ResourceWeightDictionary weightDictionary; // przelicznik ilość -> waga

    public float calculateTotalWeight(List<Module> modules, List<Resource> resources) {
        // zsumuj wagi wszystkich modułów (getWeight) oraz zasobów (używając weightDictionary.calculateWeight)
        return 0.0f;
    }

    public boolean isLimitExceeded(float totalWeight, float maxWeight) {
        // zwróć true jeśli całkowita waga przekracza limit zdefiniowany w manifeście
        return false;
    }
}