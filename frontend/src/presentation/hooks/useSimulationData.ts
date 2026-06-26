import { useState } from "react";
import type {
  ScenarioDifficulty,
  SimulationResult,
} from "../../core/application/ports/ISimulationRepository";
import { simulationRepository } from "../../infrastructure/dependencyInjection/container";

export const useSimulationData = () => {
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [scheduleId, setScheduleId] = useState<string | null>(null);
  const [draftId, setDraftId] = useState<string | null>(null);
  const [proposedEventsCount, setProposedEventsCount] = useState(0);
  const [result, setResult] = useState<SimulationResult | null>(null);

  const runAutomaticSimulation = async (input: {
    missionDuration: number;
    nominalSessionId: string;
    difficulty: ScenarioDifficulty;
  }) => {
    setIsLoading(true);
    setError(null);

    try {
      const draft = await simulationRepository.generateAutomaticScenario({
        missionDuration: input.missionDuration,
        difficulty: input.difficulty,
      });
      setDraftId(draft.draftId);
      setProposedEventsCount(draft.proposedEventsCount);

      const schedule = await simulationRepository.approveScenarioDraft(draft.draftId);
      setScheduleId(schedule.scheduleId);

      const simulation = await simulationRepository.runScenarioSimulation({
        nominalSessionId: input.nominalSessionId,
        scheduleId: schedule.scheduleId,
      });
      setResult(simulation);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Nieznany błąd symulacji.");
    } finally {
      setIsLoading(false);
    }
  };

  const runManualSimulation = async (input: {
    missionDuration: number;
    nominalSessionId: string;
  }) => {
    setIsLoading(true);
    setError(null);

    try {
      const schedule = await simulationRepository.createManualSchedule({
        missionDuration: input.missionDuration,
      });
      setScheduleId(schedule.scheduleId);

      const simulation = await simulationRepository.runScenarioSimulation({
        nominalSessionId: input.nominalSessionId,
        scheduleId: schedule.scheduleId,
      });
      setResult(simulation);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Nieznany błąd symulacji.");
    } finally {
      setIsLoading(false);
    }
  };

  return {
    isLoading,
    error,
    scheduleId,
    draftId,
    proposedEventsCount,
    result,
    runAutomaticSimulation,
    runManualSimulation,
  };
};
