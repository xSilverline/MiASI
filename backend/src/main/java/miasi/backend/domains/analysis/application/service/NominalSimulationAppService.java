package miasi.backend.domains.analysis.application.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import miasi.backend.domains.analysis.application.port.in.IRunNominalSimulationUseCase;
import miasi.backend.domains.analysis.application.port.in.RunNominalSimulationCommand;
import miasi.backend.domains.analysis.application.port.out.IAnalysisEventPublisherPort;
import miasi.backend.domains.analysis.application.port.out.ISimulationDataProviderPort;
import miasi.backend.domains.analysis.application.port.out.ISimulationSessionRepositoryPort;
import miasi.backend.domains.analysis.domain._payload.PayloadOptimizationSession;
import miasi.backend.domains.analysis.domain._simulation.NominalSimulationCompletedEvent;
import miasi.backend.domains.analysis.domain._simulation.NominalSimulationSession;
import miasi.backend.domains.analysis.domain._simulation.SimulationOutcomeEvaluator;
import miasi.backend.domains.analysis.domain._simulation.SimulationVariant;
import miasi.backend.domains.analysis.domain._simulation.TimelineSimulator;
import miasi.backend.domains.analysis.domain.core.DailyState;
import miasi.backend.domains.analysis.domain.core.VariantType;

@RequiredArgsConstructor
public class NominalSimulationAppService implements IRunNominalSimulationUseCase {

  private final ISimulationDataProviderPort dataProvider;
  private final ISimulationSessionRepositoryPort sessionRepository;

  private final TimelineSimulator timelineSimulator;
  private final SimulationOutcomeEvaluator outcomeEvaluator;
  private final IAnalysisEventPublisherPort eventPublisher;


  @Override
  public NominalSimulationSession simulate(RunNominalSimulationCommand command) {

    PayloadOptimizationSession baseSession = dataProvider.getPayloadSession(
        command.payloadSessionId());

    List<DailyState> timeline = timelineSimulator.simulate(
        baseSession.getInputManifest(),
        command.customizedModules(),
        command.customizedSupplies()
    );

    var outcome = outcomeEvaluator.evaluate(timeline, baseSession.getInputManifest());
    SimulationVariant nominalVariant = new SimulationVariant(VariantType.IDEAL, timeline, outcome);

    NominalSimulationSession session = new NominalSimulationSession(
        UUID.randomUUID().toString(),
        command.payloadSessionId(),
        command.customizedModules(),
        command.customizedSupplies(),
        nominalVariant,
        LocalDateTime.now()
    );

    // EDA
    eventPublisher.publishNominalSimulationCompleted(
        new NominalSimulationCompletedEvent(session)
    );

    sessionRepository.saveNominal(session);
    return session;
  }
}