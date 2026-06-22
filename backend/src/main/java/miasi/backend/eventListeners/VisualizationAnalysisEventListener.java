package miasi.backend.eventListeners;

import java.util.function.Consumer;
import miasi.backend.domains.analysis.baseline.BaselineAnalysisCompletedEvent;
import miasi.backend.domains.analysis.simulation.MissionFailureDetectedEvent;
import miasi.backend.domains.analysis.simulation.SimulationAnalysisCompletedEvent;
import miasi.backend.domains.visualization.VisualizationAnalysisEventInbox;
import miasi.backend.sharedkernel.events.IntegrationEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class VisualizationAnalysisEventListener {
  private final VisualizationAnalysisEventInbox inbox;
  private final Consumer<IntegrationEvent> processor;

  @Autowired
  public VisualizationAnalysisEventListener(VisualizationAnalysisEventInbox inbox) {
    this(inbox, ignored -> {});
  }

  VisualizationAnalysisEventListener(
      VisualizationAnalysisEventInbox inbox, Consumer<IntegrationEvent> processor) {
    this.inbox = inbox;
    this.processor = processor;
  }

  @EventListener
  public void onBaselineAnalysisCompleted(BaselineAnalysisCompletedEvent event) {
    inbox.handle(event, processor);
  }

  @EventListener
  public void onSimulationAnalysisCompleted(SimulationAnalysisCompletedEvent event) {
    inbox.handle(event, processor);
  }

  @EventListener
  public void onMissionFailureDetected(MissionFailureDetectedEvent event) {
    inbox.handle(event, processor);
  }
}
