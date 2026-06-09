package miasi.backend.domains.analisis.simulation;

import lombok.RequiredArgsConstructor;
import miasi.backend.domains.analisis.baseline.BaselineAnalysisSession;
import miasi.backend.domains.analisis.services.TimelineSimulator;
import miasi.backend.domains.analisis.types.schedule.Threat;

import java.util.List;

@RequiredArgsConstructor
public class SimulationAnalyzer {

  private final TimelineSimulator timelineSimulator;

  public SimulationAnalysisSession analyze(BaselineAnalysisSession baselineSession, List<Threat> threats) {
    // wygeneruj dwa warianty osi czasu (IDEAL oraz REAL z awariami) przez TimelineSimulator i zwróć jako SimulationAnalysisSession
    return null;
  }
}