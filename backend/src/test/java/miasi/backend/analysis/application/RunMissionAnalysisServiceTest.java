package miasi.backend.analysis.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import miasi.backend.analysis.application.model.MissionAnalysisResult;
import miasi.backend.analysis.application.port.out.AnalysisEventPublisherPort;
import miasi.backend.analysis.application.port.out.AnalysisResultRepositoryPort;
import miasi.backend.analysis.application.service.RunMissionAnalysisService;
import miasi.backend.analysis.domain.model.baseline.BaselineAnalysisCompletedEvent;
import miasi.backend.analysis.domain.model.input.MissionManifest;
import miasi.backend.analysis.domain.model.simulation.MissionFailureDetectedEvent;
import miasi.backend.analysis.domain.model.simulation.SimulationAnalysisCompletedEvent;
import miasi.backend.analysis.domain.service.DeliveryProcessor;
import miasi.backend.analysis.domain.service.DemandCalculator;
import miasi.backend.analysis.domain.service.EnergyProcessor;
import miasi.backend.analysis.domain.service.PayloadOptimizer;
import miasi.backend.analysis.domain.service.ProductionCalculator;
import miasi.backend.analysis.domain.service.SimulationOutcomeEvaluator;
import miasi.backend.analysis.domain.service.SurvivalPredictor;
import miasi.backend.analysis.domain.service.ThreatProcessor;
import miasi.backend.analysis.domain.service.TimelineSimulator;
import miasi.backend.analysis.domain.service.WeightCalculator;
import miasi.backend.analysis.domain.service.baseline.BaselineAnalyzer;
import miasi.backend.analysis.domain.service.simulation.SimulationAnalyzer;
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
