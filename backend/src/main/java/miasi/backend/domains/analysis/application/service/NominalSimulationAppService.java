package miasi.backend.domains.analysis.application.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
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
import miasi.backend.domains.analysis.domain.core.Resource;
import miasi.backend.domains.analysis.domain.core.VariantType;
import miasi.backend.domains.analysis.domain.modules.Module;

@RequiredArgsConstructor
public class NominalSimulationAppService implements IRunNominalSimulationUseCase {

  private final ISimulationDataProviderPort dataProvider;
  private final ISimulationSessionRepositoryPort sessionRepository;

  private final TimelineSimulator timelineSimulator;
  private final SimulationOutcomeEvaluator outcomeEvaluator;
  private final IAnalysisEventPublisherPort eventPublisher;

  @Override
  public NominalSimulationSession simulate(RunNominalSimulationCommand command) {

    PayloadOptimizationSession baseSession =
        dataProvider.getPayloadSession(command.payloadSessionId());
    if (baseSession == null) {
      throw new NoSuchElementException(
          "Payload optimization session not found: " + command.payloadSessionId());
    }

    // Jeśli użytkownik przysłał modyfikacje, używamy ich. W przeciwnym razie bierzemy zoptymalizowane.
    List<Module> modulesToUse =
        (command.customizedModules() != null && !command.customizedModules().isEmpty())
            ? command.customizedModules()
            : baseSession.getConfiguration().getOptimalModules();

    List<Resource> suppliesToUse =
        (command.customizedSupplies() != null && !command.customizedSupplies().isEmpty())
            ? command.customizedSupplies()
            : baseSession.getConfiguration().getStartingResources();

    // Uruchamiamy symulację z poprawnymi danymi
    List<DailyState> timeline =
        timelineSimulator.simulate(
            baseSession.getInputManifest(),
            modulesToUse,
            suppliesToUse);

    var outcome = outcomeEvaluator.evaluate(timeline, baseSession.getInputManifest());
    SimulationVariant nominalVariant = new SimulationVariant(VariantType.IDEAL, timeline, outcome);

    // Zapisujemy wybrane/zbudowane moduły i zapasy, a nie tylko te z komendy
    NominalSimulationSession session =
        new NominalSimulationSession(
            UUID.randomUUID().toString(),
            command.payloadSessionId(),
            modulesToUse,
            suppliesToUse,
            nominalVariant,
            LocalDateTime.now());

    eventPublisher.publishNominalSimulationCompleted(new NominalSimulationCompletedEvent(session));

    sessionRepository.saveNominal(session);
    return session;
  }
}
