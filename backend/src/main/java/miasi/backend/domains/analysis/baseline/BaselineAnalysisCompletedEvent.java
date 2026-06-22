package miasi.backend.domains.analysis.baseline;

import java.util.List;
import java.util.UUID;
import miasi.backend.domains.analysis.types.core.DailyState;
import miasi.backend.domains.analysis.types.result.OptimalConfiguration;
import miasi.backend.sharedkernel.events.EventEnvelope;
import miasi.backend.sharedkernel.events.IntegrationEvent;

public record BaselineAnalysisCompletedEvent(
    EventEnvelope envelope,
    UUID manifestId,
    List<DailyState> baselineTimeline,
    OptimalConfiguration configuration)
    implements IntegrationEvent {

  public static BaselineAnalysisCompletedEvent create(
      UUID manifestId, List<DailyState> baselineTimeline, OptimalConfiguration configuration) {
    return new BaselineAnalysisCompletedEvent(
        EventEnvelope.initial(manifestId.toString()), manifestId, baselineTimeline, configuration);
  }

  @Override
  public String eventType() {
    return "BaselineAnalysisCompleted";
  }
}
