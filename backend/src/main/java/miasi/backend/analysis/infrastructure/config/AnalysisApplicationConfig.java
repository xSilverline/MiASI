package miasi.backend.analysis.infrastructure.config;

import miasi.backend.analysis.application.port.out.AnalysisEventPublisherPort;
import miasi.backend.analysis.application.port.out.AnalysisResultRepositoryPort;
import miasi.backend.analysis.application.service.RunMissionAnalysisService;
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
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AnalysisApplicationConfig {

  @Bean
  public RunMissionAnalysisService runMissionAnalysisService(
      AnalysisResultRepositoryPort resultRepository, AnalysisEventPublisherPort eventPublisher) {
    AnalysisGraph graph = buildAnalysisGraph();
    return new RunMissionAnalysisService(
        graph.baselineAnalyzer(), graph.simulationAnalyzer(), resultRepository, eventPublisher);
  }

  private AnalysisGraph buildAnalysisGraph() {
    DemandCalculator demandCalculator = new DemandCalculator();
    ProductionCalculator productionCalculator = new ProductionCalculator();
    DeliveryProcessor deliveryProcessor = new DeliveryProcessor();
    ThreatProcessor threatProcessor = new ThreatProcessor();
    EnergyProcessor energyProcessor = new EnergyProcessor(productionCalculator, demandCalculator);
    SurvivalPredictor survivalPredictor =
        new SurvivalPredictor(demandCalculator, productionCalculator);
    TimelineSimulator timelineSimulator =
        new TimelineSimulator(
            demandCalculator,
            productionCalculator,
            deliveryProcessor,
            threatProcessor,
            energyProcessor,
            survivalPredictor);
    PayloadOptimizer payloadOptimizer =
        new PayloadOptimizer(new WeightCalculator(), timelineSimulator);
    BaselineAnalyzer baselineAnalyzer = new BaselineAnalyzer(payloadOptimizer, timelineSimulator);
    SimulationAnalyzer simulationAnalyzer =
        new SimulationAnalyzer(timelineSimulator, new SimulationOutcomeEvaluator());

    return new AnalysisGraph(baselineAnalyzer, simulationAnalyzer);
  }

  private record AnalysisGraph(
      BaselineAnalyzer baselineAnalyzer, SimulationAnalyzer simulationAnalyzer) {}
}
