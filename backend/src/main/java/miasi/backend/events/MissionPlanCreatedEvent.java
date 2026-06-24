package miasi.backend.events;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import miasi.backend.domains.configuration.missionPlan.MissionPlan;
import org.springframework.context.ApplicationEvent;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MissionPlanCreatedEvent extends ApplicationEvent {
  int missionPlanId;
  MissionPlan missionPlan;

  public MissionPlanCreatedEvent(Object source) {
    this(-1, (MissionPlan) source);
  }

  public MissionPlanCreatedEvent(int missionPlanId, MissionPlan missionPlan) {
    super(missionPlan);
    this.missionPlanId = missionPlanId;
    this.missionPlan = missionPlan;
  }
}
