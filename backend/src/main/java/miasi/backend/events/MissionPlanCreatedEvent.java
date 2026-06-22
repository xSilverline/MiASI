package miasi.backend.events;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import miasi.backend.domains.configuration.missionPlan.MissionPlan;
import org.springframework.context.ApplicationEvent;


@FieldDefaults(level = AccessLevel.PRIVATE)
public class MissionPlanCreatedEvent extends ApplicationEvent {
  @Getter
  MissionPlan missionPlan;

  public MissionPlanCreatedEvent(Object source) {
    super(source);
    this.missionPlan = (MissionPlan) source;
  }
}
