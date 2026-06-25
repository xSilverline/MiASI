package miasi.backend.domains.schedule;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import miasi.backend.domains.schedule.enums.ScheduleStatus;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
public class MissionSchedule {
  String id;
  String missionPlanId;
  int durationSols;
  ScheduleStatus status;
  List<ScheduledEvent> events;

  public static MissionSchedule createDraft(String missionPlanId, int durationSols) {
    if (missionPlanId == null || missionPlanId.isBlank()) {
      throw new IllegalArgumentException("Mission plan id is required");
    }
    if (durationSols < 1) {
      throw new IllegalArgumentException("Mission duration must be at least 1 sol");
    }

    return new MissionSchedule(
        UUID.randomUUID().toString(),
        missionPlanId,
        durationSols,
        ScheduleStatus.DRAFT,
        new ArrayList<>());
  }

  public void addEvent(ScheduledEvent event) {
    validateEvent(event);
    ensureEventsList();
    events.add(event);
  }

  public void updateEvent(String eventId, ScheduledEvent event) {
    validateEventId(eventId);
    validateEvent(event);
    ensureEventsList();

    for (int index = 0; index < events.size(); index++) {
      if (eventId.equals(events.get(index).getId())) {
        events.set(index, event);
        return;
      }
    }

    throw new IllegalArgumentException("Scheduled event not found: " + eventId);
  }

  public void removeEvent(String eventId) {
    validateEventId(eventId);
    ensureEventsList();

    boolean removed = events.removeIf(event -> eventId.equals(event.getId()));
    if (!removed) {
      throw new IllegalArgumentException("Scheduled event not found: " + eventId);
    }
  }

  public void approveScenario(ScenarioDraft draft) {
    if (draft == null) {
      throw new IllegalArgumentException("Scenario draft is required");
    }
    List<ScheduledEvent> draftEvents = draft.getProposedEvents();
    events = draftEvents == null ? new ArrayList<>() : new ArrayList<>(draftEvents);
    status = ScheduleStatus.READY_FOR_ANALYSIS;
  }

  public MissionTimeline timeline() {
    ensureEventsList();
    List<ScheduledEvent> sortedEvents =
        events.stream().sorted(Comparator.comparingInt(ScheduledEvent::getSol)).toList();
    return new MissionTimeline(sortedEvents);
  }

  private void ensureEventsList() {
    if (events == null) {
      events = new ArrayList<>();
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
