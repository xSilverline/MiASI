package miasi.backend.domains.analysis.infrastructure.in.web.dto;

import miasi.backend.domains.analysis.domain._simulation.NominalSimulationSession;
import miasi.backend.domains.analysis.domain._simulation.SimulationVariant;

public record NominalSimulationResponse(String sessionId, SimulationVariant nominalVariant) {
  public static NominalSimulationResponse fromDomain(NominalSimulationSession session) {
    return new NominalSimulationResponse(session.getId(), session.getNominalVariant());
  }
}
