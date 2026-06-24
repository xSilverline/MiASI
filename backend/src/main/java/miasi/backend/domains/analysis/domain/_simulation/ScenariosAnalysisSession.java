package miasi.backend.domains.analysis.domain._simulation;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Value;
import miasi.backend.domains.analysis.domain.schedule.Threat;

@Value
public class ScenariosAnalysisSession {

  String id;
  String nominalSessionId;             // Odniesienie do Fazy 2 (ostatecznie zatwierdzony układ)
  String scheduleId;                   // Skąd wzięliśmy zagrożenia
  List<Threat> appliedThreats;         // WEJŚCIE: Jakie awarie uderzyły w bazę
  SimulationVariant idealVariant;      // Skopiowane z Fazy 2 (żeby frontend miał to w 1 JSONie)
  SimulationVariant realVariant;       // Nowa oś czasu uwzględniająca appliedThreats
  LocalDateTime createdAt;
}