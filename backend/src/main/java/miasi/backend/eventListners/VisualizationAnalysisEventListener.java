package miasi.backend.eventListners;

import lombok.RequiredArgsConstructor;
import miasi.backend.domains.analisis.baseline.BaselineAnalysisCompletedEvent;
import miasi.backend.domains.analisis.simulation.MissionFailureDetectedEvent;
import miasi.backend.domains.analisis.simulation.SimulationAnalysisCompletedEvent;
import miasi.backend.domains.visualization.VisualizationAnalysisEventInbox;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class VisualizationAnalysisEventListener {
  private final VisualizationAnalysisEventInbox inbox;

  @EventListener
  public void onBaselineAnalysisCompleted(BaselineAnalysisCompletedEvent event) {
    inbox.record(event);
  }

  @EventListener
  public void onSimulationAnalysisCompleted(SimulationAnalysisCompletedEvent event) {
    inbox.record(event);
  }

  @EventListener
  public void onMissionFailureDetected(MissionFailureDetectedEvent event) {
    inbox.record(event);
  }
}
