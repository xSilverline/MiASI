package miasi.backend.domains.analysis.application.port.in;

import miasi.backend.domains.analysis.domain._simulation.ScenariosAnalysisSession;

public interface IRunScenariosSimulationUseCase {

  ScenariosAnalysisSession simulate(RunScenariosSimulationCommand command);
}