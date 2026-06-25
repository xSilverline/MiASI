package miasi.backend.domains.analysis.infrastructure.config;

import miasi.backend.domains.analysis.application.port.out.IAnalysisEventPublisherPort;
import miasi.backend.domains.analysis.application.port.out.IMissionDataProviderPort;
import miasi.backend.domains.analysis.application.port.out.IPayloadSessionRepositoryPort;
import miasi.backend.domains.analysis.application.port.out.ISimulationDataProviderPort;
import miasi.backend.domains.analysis.application.port.out.ISimulationSessionRepositoryPort;
import miasi.backend.domains.analysis.application.service.NominalSimulationAppService;
import miasi.backend.domains.analysis.application.service.PayloadAppService;
import miasi.backend.domains.analysis.application.service.ScenariosSimulationAppService;
import miasi.backend.domains.analysis.domain._payload.PayloadWeightOptimizer;
import miasi.backend.domains.analysis.domain._payload.WeightCalculator;
import miasi.backend.domains.analysis.domain._simulation.ScenariosSimulator;
import miasi.backend.domains.analysis.domain._simulation.SimulationOutcomeEvaluator;
import miasi.backend.domains.analysis.domain._simulation.TimelineSimulator;
import miasi.backend.domains.analysis.domain.crew.DemandCalculator;
import miasi.backend.domains.analysis.domain.crew.SurvivalPredictor;
import miasi.backend.domains.analysis.domain.energy.PowerGridPlanner;
import miasi.backend.domains.analysis.domain.energy.PowerGridSimulator;
import miasi.backend.domains.analysis.domain.modules.ProductionCalculator;
import miasi.backend.domains.analysis.domain.schedule.DeliveryProcessor;
import miasi.backend.domains.analysis.domain.schedule.ThreatProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = "miasi.backend.domains.analysis")
public class AnalysisConfig {

  // === DOMAIN CALCULATORS ===

  @Bean
  public DemandCalculator demandCalculator() {
    return new DemandCalculator();
  }

  @Bean
  public ProductionCalculator productionCalculator() {
    return new ProductionCalculator();
  }

  @Bean
  public DeliveryProcessor deliveryProcessor() {
    return new DeliveryProcessor();
  }

  @Bean
  public ThreatProcessor threatProcessor() {
    return new ThreatProcessor();
  }

  @Bean
  public PowerGridSimulator powerGridSimulator(
      ProductionCalculator prodCalc, DemandCalculator demandCalc) {
    return new PowerGridSimulator(prodCalc, demandCalc);
  }

  @Bean
  public SurvivalPredictor survivalPredictor(
      DemandCalculator demandCalc, ProductionCalculator prodCalc) {
    return new SurvivalPredictor(demandCalc, prodCalc);
  }

  @Bean
  public WeightCalculator weightCalculator() {
    return new WeightCalculator();
  }

  @Bean
  public PowerGridPlanner powerGridPlanner(
      ProductionCalculator prodCalc, DemandCalculator demandCalc) {
    return new PowerGridPlanner(prodCalc, demandCalc);
  }

  // === DOMAIN "ENGINES" ===

  @Bean
  public PayloadWeightOptimizer payloadWeightOptimizer(
      WeightCalculator weightCalculator,
      TimelineSimulator timelineSimulator,
      PowerGridPlanner powerGridPlanner,
      SimulationOutcomeEvaluator evaluato) {
    return new PayloadWeightOptimizer(
        weightCalculator, timelineSimulator, powerGridPlanner, evaluato);
  }

  @Bean
  public SimulationOutcomeEvaluator simulationOutcomeEvaluator() {
    return new SimulationOutcomeEvaluator();
  }

  @Bean
  public TimelineSimulator timelineSimulator(
      DemandCalculator demandCalculator,
      ProductionCalculator productionCalculator,
      DeliveryProcessor deliveryProcessor,
      ThreatProcessor threatProcessor,
      PowerGridSimulator powerGridSimulator,
      SurvivalPredictor survivalPredictor) {

    return new TimelineSimulator(
        demandCalculator,
        productionCalculator,
        deliveryProcessor,
        threatProcessor,
        powerGridSimulator,
        survivalPredictor);
  }

  @Bean
  public ScenariosSimulator scenariosSimulator(
      TimelineSimulator timelineSimulator, SimulationOutcomeEvaluator outcomeEvaluator) {

    return new ScenariosSimulator(timelineSimulator, outcomeEvaluator);
  }

  // === APP SERVICE ===

  @Bean
  public PayloadAppService payloadAppService(
      IMissionDataProviderPort dataProvider,
      IPayloadSessionRepositoryPort sessionRepository,
      PayloadWeightOptimizer optimizer,
      IAnalysisEventPublisherPort eventPublisher) {

    return new PayloadAppService(dataProvider, sessionRepository, optimizer, eventPublisher);
  }

  @Bean
  public NominalSimulationAppService nominalSimulationAppService(
      ISimulationDataProviderPort dataProvider,
      ISimulationSessionRepositoryPort sessionRepository,
      TimelineSimulator timelineSimulator,
      SimulationOutcomeEvaluator outcomeEvaluator,
      IAnalysisEventPublisherPort eventPublisher) {

    return new NominalSimulationAppService(
        dataProvider, sessionRepository, timelineSimulator, outcomeEvaluator, eventPublisher);
  }

  @Bean
  public ScenariosSimulationAppService scenariosSimulationAppService(
      ISimulationDataProviderPort dataProvider,
      ISimulationSessionRepositoryPort sessionRepository,
      IAnalysisEventPublisherPort eventPublisher,
      ScenariosSimulator scenariosSimulator) {

    return new ScenariosSimulationAppService(
        dataProvider, sessionRepository, eventPublisher, scenariosSimulator);
  }
}
