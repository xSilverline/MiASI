package miasi.backend.domains.configuration.missionPlan;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import miasi.backend.domains.configuration.modules.Module;
import miasi.backend.domains.configuration.other.AtmosphereComposition;
import miasi.backend.domains.configuration.other.Resources;
import miasi.backend.domains.configuration.other.SexProfile;
import miasi.backend.enums.ResourceType;
import miasi.backend.events.MissionPlanCreatedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
public class MissionPlan {
  String uuid;
  List<SexProfile> crew;
  int missionDurationSols;
  List<Resources> startingResources;
  List<Module> modules;
  AtmosphereComposition atmosphereComposition;
  float maxStartingWeight;

  @Autowired
  private ApplicationEventPublisher applicationEventPublisher;

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
    atmosphereComposition = new AtmosphereComposition();
    maxStartingWeight = 0;
  }

  void AddSexProfile(SexProfile profile) {
    crew.add(profile);
  }

  void addModule(Module module) {
    modules.add(module);
  }

  public void throwCreatedEvent() {
    applicationEventPublisher.publishEvent(new MissionPlanCreatedEvent(this));
  }
}
