package miasi.backend.domains.analysis.application.port.out;

import java.util.List;
import miasi.backend.domains.analysis.domain._payload.PayloadOptimizationSession;
import miasi.backend.domains.analysis.domain._simulation.NominalSimulationSession;
import miasi.backend.domains.analysis.domain.schedule.Threat;

public interface ISimulationDataProviderPort {

  // Pobiera wynik Fazy 1 (potrzebne na start Fazy 2)
  PayloadOptimizationSession getPayloadSession(String payloadSessionId);

  // Pobiera wynik Fazy 2 (potrzebne na start Fazy 3)
  NominalSimulationSession getNominalSession(String nominalSessionId);

  // Pobiera listę zagrożeń z modułu Schedule
  List<Threat> getThreatsForSchedule(String scheduleId);
}