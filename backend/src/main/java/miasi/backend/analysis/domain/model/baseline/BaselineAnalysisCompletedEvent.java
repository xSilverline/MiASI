package miasi.backend.analysis.domain.model.baseline;

import java.util.List;
import java.util.UUID;
import miasi.backend.analysis.domain.model.core.DailyState;
import miasi.backend.analysis.domain.model.result.OptimalConfiguration;
import miasi.backend.common.domain.model.event.EventEnvelope;
import miasi.backend.common.domain.model.event.IntegrationEvent;

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
