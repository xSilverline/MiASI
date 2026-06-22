package miasi.backend.domains.analysis.simulation;

import java.util.UUID;
import miasi.backend.sharedkernel.events.EventEnvelope;
import miasi.backend.sharedkernel.events.IntegrationEvent;

public record MissionFailureDetectedEvent(
    EventEnvelope envelope, UUID manifestId, SimulationVariant realVariant)
    implements IntegrationEvent {

  public static MissionFailureDetectedEvent create(UUID manifestId, SimulationVariant realVariant) {
    return new MissionFailureDetectedEvent(
        EventEnvelope.initial(manifestId.toString()), manifestId, realVariant);
  }

  @Override
  public String eventType() {
    return "MissionFailureDetected";
  }
}
