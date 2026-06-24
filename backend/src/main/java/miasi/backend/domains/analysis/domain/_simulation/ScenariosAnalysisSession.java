package miasi.backend.domains.analysis.domain._simulation;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Value;
import miasi.backend.domains.analysis.domain.schedule.Threat;

@Value
public class ScenariosAnalysisSession {

  String id;
  String nominalSessionId;
  String scheduleId;
  List<Threat> appliedThreats;
  SimulationVariant idealVariant;
  SimulationVariant realVariant;
  LocalDateTime createdAt;
}