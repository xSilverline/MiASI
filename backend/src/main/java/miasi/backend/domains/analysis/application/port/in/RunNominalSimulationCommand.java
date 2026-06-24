package miasi.backend.domains.analysis.application.port.in;

import java.util.List;
import miasi.backend.domains.analysis.domain.core.Resource;
import miasi.backend.domains.analysis.domain.modules.Module;

public record RunNominalSimulationCommand(
    String payloadSessionId,           // ID wyliczonej Fazy 1 (z jakiego punktu startujemy)
    List<Module> customizedModules,    // To, co użytkownik zmienił
    List<Resource> customizedSupplies  // To, co użytkownik zmienił
) {}