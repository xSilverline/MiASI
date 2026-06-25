package miasi.backend.domains.analysis.application.port.in;

import miasi.backend.domains.analysis.domain._simulation.NominalSimulationSession;

public interface IRunNominalSimulationUseCase {

  NominalSimulationSession simulate(RunNominalSimulationCommand command);
}
