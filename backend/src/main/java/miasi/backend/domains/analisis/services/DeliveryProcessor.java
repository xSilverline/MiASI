package miasi.backend.domains.analisis.services;

import miasi.backend.domains.analisis.types.core.Resource;
import miasi.backend.domains.analisis.types.modules.Module;
import miasi.backend.domains.analisis.types.schedule.Delivery;

import java.util.List;

public class DeliveryProcessor {

    public void process(int currentSol, List<Delivery> deliveries, List<Module> currentModules, List<Resource> warehouse) {
        // jeśli na dzisiejszy sol przypada dostawa: dodaj jej moduły do currentModules i surowce do warehouse
    }
}