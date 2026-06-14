package miasi.backend.domains.schedule;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import miasi.backend.enums.EventType;
import org.junit.jupiter.api.Test;

class MissionTimelineTest {

  @Test
  void filterByType_shouldReturnOnlyEventsOfSelectedType() {
    ScheduledEvent threat = event("event-1", EventType.THREAT, 1);
    ScheduledEvent delivery = event("event-2", EventType.SUPPLY_DELIVERY, 2);
    ScheduledEvent secondThreat = event("event-3", EventType.THREAT, 3);
    MissionTimeline timeline = new MissionTimeline(List.of(threat, delivery, secondThreat));

    MissionTimeline filtered = timeline.filterByType(EventType.THREAT);

    assertEquals(List.of(threat, secondThreat), filtered.getEventsSortedBySol());
  }

  private ScheduledEvent event(String id, EventType type, int sol) {
    return new ScheduledEvent(id, type, sol, "description");
  }
}
