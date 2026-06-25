package miasi.backend.domains.analysis.application.port.out;

import java.util.List;
import miasi.backend.domains.analysis.domain._payload.PayloadOptimizationSession;
import miasi.backend.domains.analysis.domain._simulation.NominalSimulationSession;
import miasi.backend.domains.analysis.domain.schedule.Threat;

public interface ISimulationDataProviderPort {

  PayloadOptimizationSession getPayloadSession(String payloadSessionId);

  NominalSimulationSession getNominalSession(String nominalSessionId);

  List<Threat> getThreatsForSchedule(String scheduleId);
}
