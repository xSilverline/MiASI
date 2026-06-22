package miasi.backend.domains.analysis.types.input;

import java.util.List;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import miasi.backend.domains.analysis.types.crew.CrewGroup;
import miasi.backend.domains.analysis.types.modules.Module;
import miasi.backend.domains.analysis.types.schedule.Delivery;
import miasi.backend.domains.analysis.types.schedule.Threat;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
public class MissionManifest {
  UUID id;
  int durationSols;
  int rescueSols;
  float maxWeightSolZero;

  List<CrewGroup> crew;
  List<Module> catalog;
  List<Delivery> deliveries;
  List<Threat> threats;
}
