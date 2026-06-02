package miasi.backend.domains.analisis.simulation;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@AllArgsConstructor
public class SimulationAnalysisCompletedEvent {
    UUID manifestId;
    SimulationVariant idealVariant; // scenariusz bez awarii (linia bazowa)
    SimulationVariant realVariant;  // scenariusz uwzględniający awarie i spadki wydajności
}