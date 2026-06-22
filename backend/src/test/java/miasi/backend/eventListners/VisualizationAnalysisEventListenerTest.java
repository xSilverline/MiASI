package miasi.backend.eventListners;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.UUID;
import miasi.backend.domains.analisis.baseline.BaselineAnalysisCompletedEvent;
import miasi.backend.domains.analisis.simulation.MissionFailureDetectedEvent;
import miasi.backend.domains.analisis.simulation.SimulationAnalysisCompletedEvent;
import miasi.backend.domains.visualization.VisualizationAnalysisEventInbox;
import org.junit.jupiter.api.Test;

class VisualizationAnalysisEventListenerTest {
  @Test
  void shouldRecordAnalysisEventsForVisualization() {
    VisualizationAnalysisEventInbox inbox = new VisualizationAnalysisEventInbox();
    VisualizationAnalysisEventListener listener = new VisualizationAnalysisEventListener(inbox);
    BaselineAnalysisCompletedEvent baselineEvent =
        new BaselineAnalysisCompletedEvent(UUID.randomUUID(), null, null);
    SimulationAnalysisCompletedEvent simulationEvent =
        new SimulationAnalysisCompletedEvent(UUID.randomUUID(), null, null);
    MissionFailureDetectedEvent failureEvent =
        new MissionFailureDetectedEvent(UUID.randomUUID(), null);

    listener.onBaselineAnalysisCompleted(baselineEvent);
    listener.onSimulationAnalysisCompleted(simulationEvent);
    listener.onMissionFailureDetected(failureEvent);

    assertEquals(3, inbox.getReceivedEvents().size());
    assertEquals(baselineEvent, inbox.getReceivedEvents().get(0));
    assertEquals(simulationEvent, inbox.getReceivedEvents().get(1));
    assertEquals(failureEvent, inbox.getReceivedEvents().get(2));
  }
}
