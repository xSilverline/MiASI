package schedule.domain;

import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import schedule.domain.enums.EventType;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
public class MissionTimeline {
  List<ScheduledEvent> eventsSortedBySol;

  public MissionTimeline filterByType(EventType type) {
    return null;
  }
}
