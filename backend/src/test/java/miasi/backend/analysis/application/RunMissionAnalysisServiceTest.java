package miasi.backend.analysis.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import miasi.backend.analysis.application.port.out.AnalysisEventPublisherPort;
import miasi.backend.analysis.application.port.out.AnalysisResultRepositoryPort;
import miasi.backend.domains.analysis.baseline.BaselineAnalysisCompletedEvent;
import miasi.backend.domains.analysis.baseline.BaselineAnalyzer;
import miasi.backend.domains.analysis.services.DeliveryProcessor;
import miasi.backend.domains.analysis.services.DemandCalculator;
import miasi.backend.domains.analysis.services.EnergyProcessor;
import miasi.backend.domains.analysis.services.PayloadOptimizer;
import miasi.backend.domains.analysis.services.ProductionCalculator;
import miasi.backend.domains.analysis.services.SimulationOutcomeEvaluator;
import miasi.backend.domains.analysis.services.SurvivalPredictor;
import miasi.backend.domains.analysis.services.ThreatProcessor;
import miasi.backend.domains.analysis.services.TimelineSimulator;
import miasi.backend.domains.analysis.services.WeightCalculator;
import miasi.backend.domains.analysis.simulation.MissionFailureDetectedEvent;
import miasi.backend.domains.analysis.simulation.SimulationAnalysisCompletedEvent;
import miasi.backend.domains.analysis.simulation.SimulationAnalyzer;
import miasi.backend.domains.analysis.types.input.MissionManifest;
import org.junit.jupiter.api.Test;

class RunMissionAnalysisServiceTest {

  @Test
  void run_shouldAnalyzePersistAndPublishCompletedEventsWithoutSpring() {
    // given
    FakeResultRepository repository = new FakeResultRepository();
    RecordingPublisher publisher = new RecordingPublisher();
    RunMissionAnalysisService service =
        new RunMissionAnalysisService(
            baselineAnalyzer(), simulationAnalyzer(), repository, publisher);
    MissionManifest manifest =
        new MissionManifest(
            UUID.randomUUID(), 2, 1, 1000f, List.of(), List.of(), List.of(), List.of());

    // when
    MissionAnalysisResult result = service.run(manifest);

    // then
    assertNotNull(result.baselineSession());
    assertNotNull(result.simulationSession());
    assertEquals("COMPLETED", result.baselineSession().getStatus());
    assertTrue(repository.findByManifestId(manifest.getId()).isPresent());
    assertEquals(2, publisher.events.size());
    assertInstanceOf(BaselineAnalysisCompletedEvent.class, publisher.events.get(0));
    assertInstanceOf(SimulationAnalysisCompletedEvent.class, publisher.events.get(1));
  }

  private BaselineAnalyzer baselineAnalyzer() {
    TimelineSimulator timelineSimulator = timelineSimulator();
    return new BaselineAnalyzer(
        new PayloadOptimizer(new WeightCalculator(), timelineSimulator), timelineSimulator);
  }

  private SimulationAnalyzer simulationAnalyzer() {
    return new SimulationAnalyzer(timelineSimulator(), new SimulationOutcomeEvaluator());
  }

  private TimelineSimulator timelineSimulator() {
    DemandCalculator demandCalculator = new DemandCalculator();
    ProductionCalculator productionCalculator = new ProductionCalculator();
    return new TimelineSimulator(
        demandCalculator,
        productionCalculator,
        new DeliveryProcessor(),
        new ThreatProcessor(),
        new EnergyProcessor(productionCalculator, demandCalculator),
        new SurvivalPredictor(demandCalculator, productionCalculator));
  }

  private static final class FakeResultRepository implements AnalysisResultRepositoryPort {
    private UUID manifestId;
    private MissionAnalysisResult result;

    @Override
    public void save(UUID manifestId, MissionAnalysisResult result) {
      this.manifestId = manifestId;
      this.result = result;
    }

    @Override
    public Optional<MissionAnalysisResult> findByManifestId(UUID manifestId) {
      if (manifestId.equals(this.manifestId)) {
        return Optional.ofNullable(result);
      }
      return Optional.empty();
    }
  }

  private static final class RecordingPublisher implements AnalysisEventPublisherPort {
    private final List<Object> events = new ArrayList<>();

    @Override
    public void publishBaselineAnalysisCompleted(BaselineAnalysisCompletedEvent event) {
      events.add(event);
    }

    @Override
    public void publishSimulationAnalysisCompleted(SimulationAnalysisCompletedEvent event) {
      events.add(event);
    }

    @Override
    public void publishMissionFailureDetected(MissionFailureDetectedEvent event) {
      events.add(event);
    }
  }
}
