package miasi.backend.analysis.domain.model.input;

import java.util.List;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import miasi.backend.analysis.domain.model.crew.CrewGroup;
import miasi.backend.analysis.domain.model.modules.Module;
import miasi.backend.analysis.domain.model.schedule.Delivery;
import miasi.backend.analysis.domain.model.schedule.Threat;

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
