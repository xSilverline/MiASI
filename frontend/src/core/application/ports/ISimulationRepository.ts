export type SimulationStatus = "SUCCESS" | "FAILURE" | "EVACUATION";
export type ScenarioDifficulty = "LEVEL_I" | "LEVEL_II" | "LEVEL_III" | "LEVEL_IV" | "LEVEL_V";
export type ScenarioMode = "MANUAL" | "AUTOMATIC";

export interface SimulationResourcePoint {
  sol: number;
  energyStore: number;
  waterStore: number;
  oxygenStore: number;
  foodStore: number;
}

export interface SimulationOutcome {
  status: SimulationStatus;
  deathSol?: number;
  evacuationSol?: number;
}

export interface SimulationResult {
  sessionId: string;
  scheduleId: string;
  idealTimeline: SimulationResourcePoint[];
  realTimeline: SimulationResourcePoint[];
  idealOutcome?: SimulationOutcome;
  realOutcome?: SimulationOutcome;
  appliedThreatsCount: number;
}

export interface ScenarioDraftSummary {
  draftId: string;
  proposedEventsCount: number;
}

export interface ISimulationRepository {
  generateAutomaticScenario(input: {
    missionDuration: number;
    difficulty: ScenarioDifficulty;
  }): Promise<ScenarioDraftSummary>;

  approveScenarioDraft(draftId: string): Promise<{ scheduleId: string }>;

  createManualSchedule(input: {
    missionDuration: number;
  }): Promise<{ scheduleId: string }>;

  runScenarioSimulation(input: {
    nominalSessionId: string;
    scheduleId: string;
  }): Promise<SimulationResult>;
}
