package miasi.backend.domains.analysis.domain._simulation;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Value;
import miasi.backend.domains.analysis.domain.core.Resource;
import miasi.backend.domains.analysis.domain.modules.Module;

@Value
public class NominalSimulationSession {

  String id;
  String payloadSessionId;
  List<Module> customizedModules;
  List<Resource> customizedSupplies;
  SimulationVariant nominalVariant;
  LocalDateTime createdAt;
}
