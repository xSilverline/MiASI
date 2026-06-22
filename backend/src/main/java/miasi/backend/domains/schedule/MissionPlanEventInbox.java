package miasi.backend.domains.schedule;

import java.util.List;
import miasi.backend.events.MissionPlanCreated;
import miasi.backend.events.MissionPlanUpdated;
import miasi.backend.sharedkernel.events.EventInboxEntry;
import miasi.backend.sharedkernel.events.InMemoryEventInbox;

public class MissionPlanEventInbox {
  private final InMemoryEventInbox<MissionPlanCreated> missionPlanCreatedInbox =
      new InMemoryEventInbox<>();
  private final InMemoryEventInbox<MissionPlanUpdated> missionPlanUpdatedInbox =
      new InMemoryEventInbox<>();

  public boolean record(MissionPlanCreated event) {
    return missionPlanCreatedInbox.record(event);
  }

  public boolean record(MissionPlanUpdated event) {
    return missionPlanUpdatedInbox.record(event);
  }

  public List<MissionPlanCreated> getMissionPlanCreatedEvents() {
    return missionPlanCreatedInbox.events();
  }

  public List<MissionPlanUpdated> getMissionPlanUpdatedEvents() {
    return missionPlanUpdatedInbox.events();
  }

  public List<EventInboxEntry<MissionPlanCreated>> getMissionPlanCreatedEntries() {
    return missionPlanCreatedInbox.entries();
  }

  public List<EventInboxEntry<MissionPlanUpdated>> getMissionPlanUpdatedEntries() {
    return missionPlanUpdatedInbox.entries();
  }

  public void clear() {
    missionPlanCreatedInbox.clear();
    missionPlanUpdatedInbox.clear();
  }
}
