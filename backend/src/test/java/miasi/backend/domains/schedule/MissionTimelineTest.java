package miasi.backend.domains.schedule;

import miasi.backend.domains.schedule.enums.EventType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MissionTimelineTest {

  private MissionTimeline timeline;
  private ScheduledEvent threat;
  private ScheduledEvent secondThreat;

  @BeforeEach
  void setUp() {
    // Given - before each test
    threat = event("event-1", EventType.THREAT, 1);
    ScheduledEvent delivery = event("event-2", EventType.SUPPLY_DELIVERY, 2);
    secondThreat = event("event-3", EventType.THREAT, 3);
    timeline = new MissionTimeline(List.of(threat, delivery, secondThreat));
  }

  @Test
  void filterByType_shouldReturnOnlyEventsOfSelectedType() {
    // When
    MissionTimeline filtered = timeline.filterByType(EventType.THREAT);

    // Then
    assertEquals(List.of(threat, secondThreat), filtered.getEventsSortedBySol());
  }

  private ScheduledEvent event(String id, EventType type, int sol) {
    return new ScheduledEvent(id, type, sol, "description");
  }

  @Test
  void filterByType_exceptionsThrowTest() {
    // When + Then (Valid data)
    assertDoesNotThrow(() -> {
      timeline.filterByType(EventType.THREAT);
    });

    // When + Then (Null as argument)
    assertThrows(IllegalArgumentException.class, () ->
        timeline.filterByType(null));

    // When + Then (Event list is null)
    timeline.setEventsSortedBySol(null);
    assertEquals(0, timeline.filterByType(EventType.THREAT).getEventsSortedBySol().size());
  }
}

