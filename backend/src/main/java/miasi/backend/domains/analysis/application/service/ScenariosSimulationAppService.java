package miasi.backend.domains.analysis.application.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import miasi.backend.domains.analysis.application.port.in.IRunScenariosSimulationUseCase;
import miasi.backend.domains.analysis.application.port.in.RunScenariosSimulationCommand;
import miasi.backend.domains.analysis.application.port.out.IAnalysisEventPublisherPort;
import miasi.backend.domains.analysis.application.port.out.ISimulationDataProviderPort;
import miasi.backend.domains.analysis.application.port.out.ISimulationSessionRepositoryPort;
import miasi.backend.domains.analysis.domain._payload.PayloadOptimizationSession;
import miasi.backend.domains.analysis.domain._simulation.NominalSimulationSession;
import miasi.backend.domains.analysis.domain._simulation.ScenariosAnalysisCompletedEvent;
import miasi.backend.domains.analysis.domain._simulation.ScenariosAnalysisSession;
import miasi.backend.domains.analysis.domain._simulation.ScenariosSimulator;
import miasi.backend.domains.analysis.domain.core.MissionManifest;
import miasi.backend.domains.analysis.domain.schedule.Threat;


@RequiredArgsConstructor
public class ScenariosSimulationAppService implements IRunScenariosSimulationUseCase {

  private final ISimulationDataProviderPort dataProvider;
  private final ISimulationSessionRepositoryPort sessionRepository;
  private final IAnalysisEventPublisherPort eventPublisher;

  // Główny silnik Fazy 3 (zmieniona nazwa zmiennej, żeby pasowała do nazwy klasy)
  private final ScenariosSimulator scenariosSimulator;

  @Override
  public ScenariosAnalysisSession simulate(RunScenariosSimulationCommand command) {

    // 1. POBIERANIE ZATWIERDZONEGO UKŁADU (FAZA 2) ORAZ AWARII
    NominalSimulationSession nominalSession = dataProvider.getNominalSession(command.nominalSessionId());
    List<Threat> threats = dataProvider.getThreatsForSchedule(command.scheduleId());

    // 2. MAGIA ARCHITEKTURY: Cofamy się do Fazy 1 po Manifest Misji!
    // Dyrygent wyciąga z NominalSession ID poprzedniej fazy, pobiera ją i bierze oryginalny dokument.
    PayloadOptimizationSession payloadSession = dataProvider.getPayloadSession(nominalSession.getPayloadSessionId());
    MissionManifest baseManifest = payloadSession.getInputManifest();

    // 3. LOGIKA DOMENOWA (Podajemy pełen komplet 4 argumentów)
    ScenariosAnalysisSession session = scenariosSimulator.analyze(
        nominalSession,
        baseManifest,
        threats,
        command.scheduleId() // Przekazujemy id harmonogramu wprost z komendy
    );

    // 4. ZAPIS DO BAZY
    sessionRepository.saveScenarios(session);

    // 5. PUBLIKACJA ZDARZENIA (Dla wizualizacji)
    eventPublisher.publishScenariosAnalysisCompleted(
        new ScenariosAnalysisCompletedEvent(session)
    );

    return session;
  }
}