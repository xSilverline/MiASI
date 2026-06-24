package miasi.backend.domains.schedule;

import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import miasi.backend.domains.schedule.enums.EventType;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
public class ScheduledEvent {
  String id;
  EventType type;
  int sol;
  String description;
  List<EventEffect> effects;

  public ScheduledEvent(String id, EventType type, int sol, String description) {
    this(id, type, sol, description, List.of());
  }

  public ScheduledEvent(
      String id, EventType type, int sol, String description, List<EventEffect> effects) {
    this.id = id;
    this.type = type;
    this.sol = sol;
    this.description = description;
    this.effects = effects == null ? List.of() : effects;
  }
}
