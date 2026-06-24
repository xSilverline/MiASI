package miasi.backend.eventListners;

import lombok.RequiredArgsConstructor;
import miasi.backend.domains.analysis.domain._payload.PayloadOptimizationCompletedEvent;
import miasi.backend.domains.analysis.domain._simulation.NominalSimulationCompletedEvent;
import miasi.backend.domains.analysis.domain._simulation.ScenariosAnalysisCompletedEvent;
import miasi.backend.domains.visualization.VisualizationAnalysisEventInbox;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class VisualizationAnalysisEventListener {

  private final VisualizationAnalysisEventInbox inbox;

  @EventListener
  public void onPayloadOptimizationCompleted(PayloadOptimizationCompletedEvent event) {
    inbox.record(event);
  }

  @EventListener
  public void onNominalSimulationCompleted(NominalSimulationCompletedEvent event) {
    inbox.record(event);
  }

  @EventListener
  public void onScenariosAnalysisCompleted(ScenariosAnalysisCompletedEvent event) {
    inbox.record(event);
  }
}
