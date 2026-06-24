package miasi.backend.eventListners;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import miasi.backend.domains.analysis.domain._payload.PayloadOptimizationCompletedEvent;
import miasi.backend.domains.analysis.domain._payload.PayloadOptimizationSession;
import miasi.backend.domains.analysis.domain._simulation.NominalSimulationCompletedEvent;
import miasi.backend.domains.analysis.domain._simulation.NominalSimulationSession;
import miasi.backend.domains.analysis.domain._simulation.ScenariosAnalysisCompletedEvent;
import miasi.backend.domains.analysis.domain._simulation.ScenariosAnalysisSession;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

class SpringAnalysisEventAdapterTest {

  PayloadOptimizationSession dummyPayloadSession = mock(PayloadOptimizationSession.class);
  NominalSimulationSession dummyNominalSession = mock(NominalSimulationSession.class);
  ScenariosAnalysisSession dummyScenariosSession = mock(ScenariosAnalysisSession.class);

  @Test
  void shouldPublishAnalysisEventsThroughSpring() {
    ApplicationEventPublisher springPublisher = mock(ApplicationEventPublisher.class);
    SpringAnalysisEventAdapter adapter = new SpringAnalysisEventAdapter(springPublisher);
    PayloadOptimizationCompletedEvent payloadOptimizationCompletedEvent =
        new PayloadOptimizationCompletedEvent(dummyPayloadSession);
    NominalSimulationCompletedEvent nominalSimulationCompletedEvent =
        new NominalSimulationCompletedEvent(dummyNominalSession);
    ScenariosAnalysisCompletedEvent scenariosAnalysisCompletedEvent =
        new ScenariosAnalysisCompletedEvent(dummyScenariosSession);

    adapter.publishPayloadOptimizationCompleted(payloadOptimizationCompletedEvent);
    adapter.publishNominalSimulationCompleted(nominalSimulationCompletedEvent);
    adapter.publishScenariosAnalysisCompleted(scenariosAnalysisCompletedEvent);

    verify(springPublisher).publishEvent(payloadOptimizationCompletedEvent);
    verify(springPublisher).publishEvent(nominalSimulationCompletedEvent);
    verify(springPublisher).publishEvent(scenariosAnalysisCompletedEvent);
  }
}
