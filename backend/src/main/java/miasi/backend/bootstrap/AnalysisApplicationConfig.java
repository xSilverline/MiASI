package miasi.backend.bootstrap;

import miasi.backend.analysis.application.RunMissionAnalysisService;
import miasi.backend.analysis.application.port.out.AnalysisEventPublisherPort;
import miasi.backend.analysis.application.port.out.AnalysisResultRepositoryPort;
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
import miasi.backend.domains.analysis.simulation.SimulationAnalyzer;
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
