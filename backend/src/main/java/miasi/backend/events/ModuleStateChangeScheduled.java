package miasi.backend.events;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import miasi.backend.domains.schedule.ModuleState;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
public class ModuleStateChangeScheduled {
  String scheduleId;
  int sol;
  String moduleId;
  ModuleState newState;
}
