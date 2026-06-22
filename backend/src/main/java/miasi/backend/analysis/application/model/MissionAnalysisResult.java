package miasi.backend.analysis.application.model;

import miasi.backend.analysis.domain.model.baseline.BaselineAnalysisSession;
import miasi.backend.analysis.domain.model.simulation.SimulationAnalysisSession;

public record MissionAnalysisResult(
    BaselineAnalysisSession baselineSession, SimulationAnalysisSession simulationSession) {}
