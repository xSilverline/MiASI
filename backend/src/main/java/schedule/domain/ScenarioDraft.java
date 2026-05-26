package schedule.domain;

import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import schedule.domain.enums.DifficultyLevel;
import schedule.domain.enums.ScenarioGenerationMode;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
public class ScenarioDraft {
  String id;
  ScenarioGenerationMode mode;
  DifficultyLevel difficulty;
  List<ScheduledEvent> proposedEvents;

  public void correctEvent(String eventId, ScheduledEvent event) {}

  public MissionSchedule approve() {
    return null;
  }
}
