package miasi.backend.domains.analisis.baseline;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import miasi.backend.domains.analisis.types.core.DailyState;
import miasi.backend.domains.analisis.types.result.OptimalConfiguration;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
public class BaselineAnalysisSession {
    UUID sessionId;
    String status; // np. "IN_PROGRESS", "COMPLETED", "FAILED"
    List<DailyState> dailyStates; // Oś czasu symulacji bazowej
    OptimalConfiguration configuration; // Wygenerowana najlżejsza konfiguracja

    public void addDailyState(DailyState state) {
        this.dailyStates.add(state);
    }
}