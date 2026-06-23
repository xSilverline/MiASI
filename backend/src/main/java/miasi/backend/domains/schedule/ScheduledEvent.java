package miasi.backend.domains.schedule;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import miasi.backend.domains.schedule.enums.EventType;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor
public class ScheduledEvent {
  String id;
  EventType type;
  int sol;
  String description;
}
