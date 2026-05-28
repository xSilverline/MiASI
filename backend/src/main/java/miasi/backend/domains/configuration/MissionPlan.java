package miasi.backend.domains.configuration;

import lombok.*;
import lombok.experimental.FieldDefaults;
import miasi.backend.domains.configuration.other.AtmosphereComposition;
import miasi.backend.domains.configuration.other.MissionPlanCreatedEvent;
import miasi.backend.domains.configuration.other.SexProfile;
import miasi.backend.enums.ResourceType;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor
public class MissionPlan {
  List<SexProfile> crew;
  int missionDurationSols;
  Map<ResourceType, Float> startingResources;
  List<Module> modules;
  AtmosphereComposition atmosphereComposition;

  void AddSexProfile(SexProfile profile) {

  }

  void addModule(Module module) {

  }

  MissionPlanCreatedEvent throwCreatedEvent() {
    return null;
  }
}
