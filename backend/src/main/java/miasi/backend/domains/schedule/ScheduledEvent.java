package miasi.backend.domains.schedule;

import lombok.*;
import lombok.experimental.FieldDefaults;
import miasi.backend.schedule.domain.EventType;

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
