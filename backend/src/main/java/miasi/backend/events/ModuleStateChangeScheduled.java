package miasi.backend.events;

import miasi.backend.sharedkernel.events.EventEnvelope;
import miasi.backend.sharedkernel.events.IntegrationEvent;
import miasi.backend.sharedkernel.model.ModuleState;

public record ModuleStateChangeScheduled(
    EventEnvelope envelope, String scheduleId, int sol, String moduleId, ModuleState newState)
    implements IntegrationEvent {

  public static ModuleStateChangeScheduled create(
      String scheduleId, int sol, String moduleId, ModuleState newState) {
    return new ModuleStateChangeScheduled(
        EventEnvelope.initial(scheduleId), scheduleId, sol, moduleId, newState);
  }

  @Override
  public String eventType() {
    return "ModuleStateChangeScheduled";
  }
}
