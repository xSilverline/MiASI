package miasi.backend.domains.analysis.simulation;

import lombok.RequiredArgsConstructor;
import miasi.backend.domains.analysis.baseline.BaselineAnalysisSession;
import miasi.backend.domains.analysis.services.TimelineSimulator;
import miasi.backend.domains.analysis.types.schedule.Threat;

import java.util.List;

@RequiredArgsConstructor
public class SimulationAnalyzer {

  private final TimelineSimulator timelineSimulator;

  public SimulationAnalysisSession analyze(BaselineAnalysisSession baselineSession, List<Threat> threats) {
    // wygeneruj dwa warianty osi czasu (IDEAL oraz REAL z awariami) przez TimelineSimulator i zwróć jako SimulationAnalysisSession
    return null;
  }
}