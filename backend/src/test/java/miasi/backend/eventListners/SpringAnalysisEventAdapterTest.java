package miasi.backend.eventListners;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.UUID;
import miasi.backend.domains.analisis.baseline.BaselineAnalysisCompletedEvent;
import miasi.backend.domains.analisis.simulation.MissionFailureDetectedEvent;
import miasi.backend.domains.analisis.simulation.SimulationAnalysisCompletedEvent;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

class SpringAnalysisEventAdapterTest {
  @Test
  void shouldPublishAnalysisEventsThroughSpring() {
    ApplicationEventPublisher springPublisher = mock(ApplicationEventPublisher.class);
    SpringAnalysisEventAdapter adapter = new SpringAnalysisEventAdapter(springPublisher);
    BaselineAnalysisCompletedEvent baselineEvent =
        new BaselineAnalysisCompletedEvent(UUID.randomUUID(), null, null);
    SimulationAnalysisCompletedEvent simulationEvent =
        new SimulationAnalysisCompletedEvent(UUID.randomUUID(), null, null);
    MissionFailureDetectedEvent failureEvent =
        new MissionFailureDetectedEvent(UUID.randomUUID(), null);

    adapter.publishBaselineAnalysisCompleted(baselineEvent);
    adapter.publishSimulationAnalysisCompleted(simulationEvent);
    adapter.publishMissionFailureDetected(failureEvent);

    verify(springPublisher).publishEvent(baselineEvent);
    verify(springPublisher).publishEvent(simulationEvent);
    verify(springPublisher).publishEvent(failureEvent);
  }
}
