package miasi.backend.analysis.domain.model.baseline;

import java.util.List;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import miasi.backend.analysis.domain.model.core.DailyState;
import miasi.backend.analysis.domain.model.result.OptimalConfiguration;

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
