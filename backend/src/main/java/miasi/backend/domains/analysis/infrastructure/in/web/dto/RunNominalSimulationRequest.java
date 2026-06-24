package miasi.backend.domains.analysis.infrastructure.in.web.dto;

import java.util.List;
import miasi.backend.domains.analysis.domain.core.Resource;
import miasi.backend.domains.analysis.domain.modules.Module;

public record RunNominalSimulationRequest(
    String payloadSessionId,
    List<Module> customizedModules,
    List<Resource> customizedSupplies
) {

}
