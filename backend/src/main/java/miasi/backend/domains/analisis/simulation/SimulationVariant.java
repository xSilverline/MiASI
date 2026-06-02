package miasi.backend.domains.analisis.simulation;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import miasi.backend.domains.analisis.types.core.DailyState;

import java.util.List;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
public class SimulationVariant {
    VariantType type;
    Status status;
    List<DailyState> timeline; // wygenerowana historia zapasów dzień po dniu
}