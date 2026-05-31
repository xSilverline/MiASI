package miasi.backend.domains.analisis.services;

import miasi.backend.domains.analisis.types.core.Resource;
import miasi.backend.domains.analisis.types.modules.Module;
import miasi.backend.domains.analisis.types.schedule.Threat;

import java.util.List;

public class ThreatProcessor {

    public void process(int currentSol, List<Threat> threats, List<Module> currentModules, List<Resource> warehouse) {
        // znajdź awarie aktywne w currentSol i zaaplikuj ich efekty na modułach (np. zmiana statusu) lub magazynie (np. wyciek)
    }
}