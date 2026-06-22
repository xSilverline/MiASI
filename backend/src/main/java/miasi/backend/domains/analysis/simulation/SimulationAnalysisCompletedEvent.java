package miasi.backend.domains.analysis.simulation;

import java.util.UUID;
import miasi.backend.sharedkernel.events.EventEnvelope;
import miasi.backend.sharedkernel.events.IntegrationEvent;

public record SimulationAnalysisCompletedEvent(
    EventEnvelope envelope,
    UUID manifestId,
    SimulationVariant idealVariant,
    SimulationVariant realVariant)
    implements IntegrationEvent {

  public static SimulationAnalysisCompletedEvent create(
      UUID manifestId, SimulationVariant idealVariant, SimulationVariant realVariant) {
    return new SimulationAnalysisCompletedEvent(
        EventEnvelope.initial(manifestId.toString()), manifestId, idealVariant, realVariant);
  }

  @Override
  public String eventType() {
    return "SimulationAnalysisCompleted";
  }
}
