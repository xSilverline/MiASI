package miasi.backend.analysis.domain.model.simulation;

import java.util.UUID;
import miasi.backend.common.domain.model.event.EventEnvelope;
import miasi.backend.common.domain.model.event.IntegrationEvent;

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
