package miasi.backend.visualization.infrastructure.in.event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;
import miasi.backend.analysis.domain.model.baseline.BaselineAnalysisCompletedEvent;
import miasi.backend.analysis.domain.model.simulation.MissionFailureDetectedEvent;
import miasi.backend.analysis.domain.model.simulation.SimulationAnalysisCompletedEvent;
import miasi.backend.visualization.domain.model.VisualizationAnalysisEventInbox;
import org.junit.jupiter.api.Test;

class VisualizationAnalysisEventListenerTest {
  @Test
  void shouldRecordAnalysisEventsForVisualization() {
    VisualizationAnalysisEventInbox inbox = new VisualizationAnalysisEventInbox();
    VisualizationAnalysisEventListener listener = new VisualizationAnalysisEventListener(inbox);
    BaselineAnalysisCompletedEvent baselineEvent =
        BaselineAnalysisCompletedEvent.create(UUID.randomUUID(), null, null);
    SimulationAnalysisCompletedEvent simulationEvent =
        SimulationAnalysisCompletedEvent.create(UUID.randomUUID(), null, null);
    MissionFailureDetectedEvent failureEvent =
        MissionFailureDetectedEvent.create(UUID.randomUUID(), null);

    listener.onBaselineAnalysisCompleted(baselineEvent);
    listener.onSimulationAnalysisCompleted(simulationEvent);
    listener.onMissionFailureDetected(failureEvent);

    assertEquals(3, inbox.getReceivedEvents().size());
    assertTrue(inbox.getReceivedEvents().contains(baselineEvent));
    assertTrue(inbox.getReceivedEvents().contains(simulationEvent));
    assertTrue(inbox.getReceivedEvents().contains(failureEvent));
  }
}
