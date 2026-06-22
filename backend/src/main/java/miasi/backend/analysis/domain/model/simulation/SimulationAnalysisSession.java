package miasi.backend.analysis.domain.model.simulation;

import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
public class SimulationAnalysisSession {
  UUID sessionId;
  String status;
  SimulationVariant idealVariant;
  SimulationVariant realVariant;
}
