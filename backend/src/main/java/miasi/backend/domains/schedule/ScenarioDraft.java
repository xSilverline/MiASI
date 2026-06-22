package miasi.backend.domains.schedule;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import miasi.backend.schedule.domain.DifficultyLevel;
import miasi.backend.schedule.domain.ScenarioGenerationMode;
import miasi.backend.schedule.domain.ScheduleStatus;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
public class ScenarioDraft {
  String id;
  String missionPlanId;
  int durationSols;
  ScenarioGenerationMode mode;
  DifficultyLevel difficulty;
  List<ScheduledEvent> proposedEvents;

  public void correctEvent(String eventId, ScheduledEvent event) {
    validateEventId(eventId);
    validateEvent(event);
    ensureProposedEvents();

    for (int index = 0; index < proposedEvents.size(); index++) {
      if (eventId.equals(proposedEvents.get(index).getId())) {
        proposedEvents.set(index, event);
        return;
      }
    }

    throw new IllegalArgumentException("Scenario draft event not found: " + eventId);
  }

  public MissionSchedule approve() {
    if (missionPlanId == null || missionPlanId.isBlank()) {
      throw new IllegalArgumentException("Mission plan id is required");
    }
    if (durationSols < 1) {
      throw new IllegalArgumentException("Mission duration must be at least 1 sol");
    }
    ensureProposedEvents();

    return new MissionSchedule(
        UUID.randomUUID().toString(),
        missionPlanId,
        durationSols,
        ScheduleStatus.READY_FOR_ANALYSIS,
        new ArrayList<>(proposedEvents));
  }

  private void ensureProposedEvents() {
    if (proposedEvents == null) {
      proposedEvents = new ArrayList<>();
    }
  }

  private void validateEvent(ScheduledEvent event) {
    if (event == null) {
      throw new IllegalArgumentException("Scheduled event is required");
    }
    validateEventId(event.getId());
  }

  private void validateEventId(String eventId) {
    if (eventId == null || eventId.isBlank()) {
      throw new IllegalArgumentException("Scheduled event id is required");
    }
  }
}
