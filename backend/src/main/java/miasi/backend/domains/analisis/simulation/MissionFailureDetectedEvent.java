package miasi.backend.domains.analisis.simulation;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@AllArgsConstructor
public class MissionFailureDetectedEvent {
  UUID manifestId;
  SimulationVariant realVariant; // przekazujemy wariant z momentem porażki
}