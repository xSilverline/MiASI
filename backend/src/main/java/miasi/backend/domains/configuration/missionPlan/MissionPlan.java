package miasi.backend.domains.configuration.missionPlan;

import java.util.ArrayList;
import java.util.List;
import lombok.*;
import lombok.experimental.FieldDefaults;
import miasi.backend.domains.configuration.modules.Module;
import miasi.backend.domains.configuration.other.Resources;
import miasi.backend.domains.configuration.other.SexProfile;
import miasi.backend.sharedkernel.model.ResourceType;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@EqualsAndHashCode
public class MissionPlan {
  List<SexProfile> crew;
  int missionDurationSols;
  List<Resources> startingResources;
  List<Module> modules;
  float maxStartingWeight;

  public MissionPlan() {
    crew = new ArrayList<>();
    crew.add(new SexProfile());
    missionDurationSols = 0;
    startingResources = new ArrayList<>();
    for (ResourceType type : ResourceType.values()) {
      startingResources.add(new Resources(type, 0));
    }
    modules = new ArrayList<>();
    modules.add(new Module());
    maxStartingWeight = 0;
  }

  void AddSexProfile(SexProfile profile) {
    crew.add(profile);
  }

  void addModule(Module module) {
    modules.add(module);
  }
}
