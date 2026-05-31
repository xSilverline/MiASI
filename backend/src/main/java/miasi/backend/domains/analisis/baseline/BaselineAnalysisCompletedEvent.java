package miasi.backend.domains.analisis.baseline;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import miasi.backend.domains.analisis.types.core.DailyState;
import miasi.backend.domains.analisis.types.result.OptimalConfiguration;

import java.util.List;
import java.util.UUID;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@AllArgsConstructor
public class BaselineAnalysisCompletedEvent {
    UUID manifestId;                     // id misji, dla której wykonano obliczenia
    List<DailyState> baselineTimeline;   // wygenerowana optymalna oś czasu
    OptimalConfiguration configuration;  // wyliczona konfiguracja ładunku i sprzętu
}