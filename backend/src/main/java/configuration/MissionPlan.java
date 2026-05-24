package configuration;

import configuration.enums.ResourceType;
import configuration.other.AtmosphereComposition;
import configuration.other.MissionPlanCreatedEvent;
import configuration.other.SexProfile;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
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
