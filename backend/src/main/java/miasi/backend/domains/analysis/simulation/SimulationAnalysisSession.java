package miasi.backend.domains.analysis.simulation;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

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