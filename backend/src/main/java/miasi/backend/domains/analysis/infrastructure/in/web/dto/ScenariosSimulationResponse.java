package miasi.backend.domains.analysis.infrastructure.in.web.dto;

import java.util.List;
import miasi.backend.domains.analysis.domain.schedule.Threat;
import miasi.backend.domains.analysis.domain._simulation.ScenariosAnalysisSession;
import miasi.backend.domains.analysis.domain._simulation.SimulationVariant;

public record ScenariosSimulationResponse(
    String sessionId,
    List<Threat> appliedThreats,
    SimulationVariant idealVariant,
    SimulationVariant realVariant
) {
  public static ScenariosSimulationResponse fromDomain(ScenariosAnalysisSession session) {
    return new ScenariosSimulationResponse(
        session.getId(),
        session.getAppliedThreats(),
        session.getIdealVariant(),
        session.getRealVariant()
    );
  }
}