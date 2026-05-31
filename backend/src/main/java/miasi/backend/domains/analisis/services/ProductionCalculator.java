package miasi.backend.domains.analisis.services;

import miasi.backend.domains.analisis.types.core.Resource;
import miasi.backend.domains.analisis.types.modules.Module;

import java.util.ArrayList;
import java.util.List;

public class ProductionCalculator {

    public List<Resource> calculateModulesProduction(List<Module> currentModules) {
        // zsumuj zasoby wyprodukowane (production) tylko z aktualnie włączonych (ACTIVE) modułów
        return new ArrayList<>();
    }
}