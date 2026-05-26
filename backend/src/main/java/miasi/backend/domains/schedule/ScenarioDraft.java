package miasi.backend.domains.schedule;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import miasi.backend.enums.DifficultyLevel;
import miasi.backend.enums.ScenarioGenerationMode;

import java.util.List;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
public class ScenarioDraft {
  String id;
  ScenarioGenerationMode mode;
  DifficultyLevel difficulty;
  List<ScheduledEvent> proposedEvents;

  public void correctEvent(String eventId, ScheduledEvent event) {
  }

  public MissionSchedule approve() {
    return null;
  }
}
