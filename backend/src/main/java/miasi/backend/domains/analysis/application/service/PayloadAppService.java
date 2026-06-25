package miasi.backend.domains.analysis.application.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import miasi.backend.domains.analysis.application.port.in.IOptimizePayloadUseCase;
import miasi.backend.domains.analysis.application.port.in.OptimizePayloadCommand;
import miasi.backend.domains.analysis.application.port.out.IAnalysisEventPublisherPort;
import miasi.backend.domains.analysis.application.port.out.IMissionDataProviderPort;
import miasi.backend.domains.analysis.application.port.out.IPayloadSessionRepositoryPort;
import miasi.backend.domains.analysis.domain._payload.OptimalConfiguration;
import miasi.backend.domains.analysis.domain._payload.PayloadOptimizationCompletedEvent;
import miasi.backend.domains.analysis.domain._payload.PayloadOptimizationSession;
import miasi.backend.domains.analysis.domain._payload.PayloadWeightOptimizer;
import miasi.backend.domains.analysis.domain.core.MissionManifest;
import miasi.backend.domains.analysis.domain.modules.Module;

@RequiredArgsConstructor
public class PayloadAppService implements IOptimizePayloadUseCase {

  private final IMissionDataProviderPort dataProvider;
  private final IPayloadSessionRepositoryPort sessionRepository;
  private final PayloadWeightOptimizer optimizer;
  private final IAnalysisEventPublisherPort eventPublisher;

  @Override
  public PayloadOptimizationSession optimize(OptimizePayloadCommand command) {

    MissionManifest manifest = dataProvider.getMissionManifest(command.missionPlanId());
    List<Module> catalog = dataProvider.getMissionModules(command.missionPlanId());

    OptimalConfiguration optimalConfig = optimizer.optimizeConfiguration(manifest, catalog);

    PayloadOptimizationSession session =
        new PayloadOptimizationSession(
            UUID.randomUUID().toString(), manifest, optimalConfig, LocalDateTime.now());

    sessionRepository.save(session);

    // EDA
    eventPublisher.publishPayloadOptimizationCompleted(
        new PayloadOptimizationCompletedEvent(session));

    return session;
  }
}
