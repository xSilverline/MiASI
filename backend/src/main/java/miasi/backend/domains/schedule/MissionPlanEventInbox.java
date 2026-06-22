package miasi.backend.domains.schedule;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import miasi.backend.events.MissionPlanCreatedEvent;

public class MissionPlanEventInbox {
  private final List<MissionPlanCreatedEvent> missionPlanCreatedEvents =
      new CopyOnWriteArrayList<>();

  public void record(MissionPlanCreatedEvent event) {
    missionPlanCreatedEvents.add(event);
  }

  public List<MissionPlanCreatedEvent> getMissionPlanCreatedEvents() {
    return List.copyOf(missionPlanCreatedEvents);
  }

  public void clear() {
    missionPlanCreatedEvents.clear();
  }
}
