package miasi.backend.domains.analysis.domain._simulation;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Value;
import miasi.backend.domains.analysis.domain.core.Resource;
import miasi.backend.domains.analysis.domain.modules.Module;

@Value
public class NominalSimulationSession {

  String id;
  String payloadSessionId;             // Odniesienie do Fazy 1 (z jakiej propozycji wyszliśmy)
  List<Module> customizedModules;      // WEJŚCIE: Co użytkownik ręcznie zmienił w maszynach
  List<Resource> customizedSupplies;   // WEJŚCIE: Co użytkownik ręcznie zmienił w zapasach
  SimulationVariant nominalVariant;    // WYNIK: Oś czasu i status (Sukces/Śmierć) bez awarii
  LocalDateTime createdAt;
}