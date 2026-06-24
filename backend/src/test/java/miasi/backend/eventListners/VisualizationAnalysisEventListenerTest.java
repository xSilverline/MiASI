package miasi.backend.eventListners;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import miasi.backend.domains.analysis.domain._payload.PayloadOptimizationCompletedEvent;
import miasi.backend.domains.analysis.domain._payload.PayloadOptimizationSession;
import miasi.backend.domains.analysis.domain._simulation.NominalSimulationCompletedEvent;
import miasi.backend.domains.analysis.domain._simulation.NominalSimulationSession;
import miasi.backend.domains.analysis.domain._simulation.ScenariosAnalysisCompletedEvent;
import miasi.backend.domains.analysis.domain._simulation.ScenariosAnalysisSession;
import miasi.backend.domains.visualization.VisualizationAnalysisEventInbox;
import org.junit.jupiter.api.Test;

class VisualizationAnalysisEventListenerTest {

  @Test
  void shouldRecordAnalysisEventsForVisualization() {
    PayloadOptimizationSession dummyPayloadSession = mock(PayloadOptimizationSession.class);
    NominalSimulationSession dummyNominalSession = mock(NominalSimulationSession.class);
    ScenariosAnalysisSession dummyScenariosSession = mock(ScenariosAnalysisSession.class);

    VisualizationAnalysisEventInbox inbox = new VisualizationAnalysisEventInbox();
    VisualizationAnalysisEventListener listener = new VisualizationAnalysisEventListener(inbox);
    PayloadOptimizationCompletedEvent payloadOptimizationCompletedEvent =
        new PayloadOptimizationCompletedEvent(dummyPayloadSession);
    NominalSimulationCompletedEvent nominalSimulationCompletedEvent =
        new NominalSimulationCompletedEvent(dummyNominalSession);
    ScenariosAnalysisCompletedEvent scenariosAnalysisCompletedEvent =
        new ScenariosAnalysisCompletedEvent(dummyScenariosSession);

    listener.onPayloadOptimizationCompleted(payloadOptimizationCompletedEvent);
    listener.onNominalSimulationCompleted(nominalSimulationCompletedEvent);
    listener.onScenariosAnalysisCompleted(scenariosAnalysisCompletedEvent);

    assertEquals(3, inbox.getReceivedEvents().size());
    assertEquals(payloadOptimizationCompletedEvent, inbox.getReceivedEvents().get(0));
    assertEquals(nominalSimulationCompletedEvent, inbox.getReceivedEvents().get(1));
    assertEquals(scenariosAnalysisCompletedEvent, inbox.getReceivedEvents().get(2));
  }
}
