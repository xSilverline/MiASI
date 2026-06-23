package miasi.backend.domains.schedule;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
public class ModuleStateChange extends ScheduledEvent {
  String moduleId;
  ModuleState newState;
}
