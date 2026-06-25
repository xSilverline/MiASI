package miasi.backend.domains.analysis.application.port.out;

import miasi.backend.domains.analysis.domain._simulation.NominalSimulationSession;
import miasi.backend.domains.analysis.domain._simulation.ScenariosAnalysisSession;

public interface ISimulationSessionRepositoryPort {

  void saveNominal(NominalSimulationSession session);

  void saveScenarios(ScenariosAnalysisSession session);
}
