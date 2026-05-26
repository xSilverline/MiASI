package miasi.backend.domains.schedule;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import miasi.backend.enums.ScheduleStatus;

import java.util.List;

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
    return null;
  }

  public void addEvent(ScheduledEvent event) {
  }

  public void updateEvent(String eventId, ScheduledEvent event) {
  }

  public void removeEvent(String eventId) {
  }

  public void approveScenario(ScenarioDraft draft) {
  }

  public MissionTimeline timeline() {
    return null;
  }
}
