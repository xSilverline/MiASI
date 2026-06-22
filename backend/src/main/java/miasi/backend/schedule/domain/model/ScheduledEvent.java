package miasi.backend.schedule.domain.model;

import lombok.*;
import lombok.experimental.FieldDefaults;

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
