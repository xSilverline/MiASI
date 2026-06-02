package miasi.backend.domains.analisis.services;

import miasi.backend.domains.analisis.types.core.Resource;
import miasi.backend.domains.analisis.types.crew.ConsumptionMode;
import miasi.backend.domains.analisis.types.crew.CrewGroup;
import miasi.backend.domains.analisis.types.modules.Module;

import java.util.ArrayList;
import java.util.List;

public class DemandCalculator {

    public List<Resource> calculateCrewDemand(List<CrewGroup> crewGroups, ConsumptionMode mode) {
        // zsumuj zapotrzebowanie każdej grupy załogi zależnie od wybranego trybu racjonowania
        return new ArrayList<>();
    }

    public List<Resource> calculateModulesDemand(List<Module> currentModules) {
        // zsumuj zasoby pobierane (consumption) tylko z aktualnie włączonych (ACTIVE) modułów
        return new ArrayList<>();
    }
}