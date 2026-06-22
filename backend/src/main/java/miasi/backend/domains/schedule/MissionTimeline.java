package miasi.backend.domains.schedule;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import miasi.backend.domains.schedule.enums.EventType;

import java.util.List;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
public class MissionTimeline {
  List<ScheduledEvent> eventsSortedBySol;

  public MissionTimeline filterByType(EventType type) {
    if (type == null) {
      throw new IllegalArgumentException("Event type is required");
    }
    if (eventsSortedBySol == null) {
      return new MissionTimeline(List.of());
    }

    return new MissionTimeline(
        eventsSortedBySol.stream().filter(event -> type.equals(event.getType())).toList());
  }
}
