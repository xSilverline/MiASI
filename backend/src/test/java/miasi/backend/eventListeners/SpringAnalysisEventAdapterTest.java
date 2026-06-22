package miasi.backend.eventListeners;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.UUID;
import miasi.backend.domains.analysis.baseline.BaselineAnalysisCompletedEvent;
import miasi.backend.domains.analysis.simulation.MissionFailureDetectedEvent;
import miasi.backend.domains.analysis.simulation.SimulationAnalysisCompletedEvent;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

class SpringAnalysisEventAdapterTest {
  @Test
  void shouldPublishAnalysisEventsThroughSpring() {
    ApplicationEventPublisher springPublisher = mock(ApplicationEventPublisher.class);
    SpringAnalysisEventAdapter adapter = new SpringAnalysisEventAdapter(springPublisher);
    BaselineAnalysisCompletedEvent baselineEvent =
        BaselineAnalysisCompletedEvent.create(UUID.randomUUID(), null, null);
    SimulationAnalysisCompletedEvent simulationEvent =
        SimulationAnalysisCompletedEvent.create(UUID.randomUUID(), null, null);
    MissionFailureDetectedEvent failureEvent =
        MissionFailureDetectedEvent.create(UUID.randomUUID(), null);

    adapter.publishBaselineAnalysisCompleted(baselineEvent);
    adapter.publishSimulationAnalysisCompleted(simulationEvent);
    adapter.publishMissionFailureDetected(failureEvent);

    verify(springPublisher).publishEvent(baselineEvent);
    verify(springPublisher).publishEvent(simulationEvent);
    verify(springPublisher).publishEvent(failureEvent);
  }
}
