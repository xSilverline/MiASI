package miasi.backend.analysis.application;

import miasi.backend.domains.analysis.baseline.BaselineAnalysisSession;
import miasi.backend.domains.analysis.simulation.SimulationAnalysisSession;

public record MissionAnalysisResult(
    BaselineAnalysisSession baselineSession, SimulationAnalysisSession simulationSession) {}
