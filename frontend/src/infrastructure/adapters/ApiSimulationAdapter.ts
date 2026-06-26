import type {
  ISimulationRepository,
  ScenarioDifficulty,
  SimulationResourcePoint,
  SimulationResult,
} from "../../core/application/ports/ISimulationRepository";

interface ApiMessageResponse {
  message: string;
}

interface ApiResource {
  type?: string;
  resourceType?: string;
  amount?: number;
  quantity?: number;
}

interface ApiDailyState {
  sol: number;
  warehouse?: ApiResource[];
}

interface ApiSimulationVariant {
  timeline?: ApiDailyState[];
  outcome?: {
    status: "SUCCESS" | "FAILURE" | "EVACUATION";
    deathSol?: number;
    evacuationSol?: number;
  };
}

interface ApiScenarioDraft {
  id: string;
  proposedEvents?: unknown[];
}

interface ApiMissionSchedule {
  id: string;
}

interface ApiScenariosSimulationResponse {
  sessionId: string;
  appliedThreats?: unknown[];
  idealVariant?: ApiSimulationVariant;
  realVariant?: ApiSimulationVariant;
}

export class ApiSimulationAdapter implements ISimulationRepository {
  private readonly API_URL = "http://localhost:8080/api";

  private getHeaders() {
    const token = localStorage.getItem("sessionToken");
    return {
      "Content-Type": "application/json",
      Authorization: token ? `Bearer ${token}` : "",
    };
  }

  private async request<T>(url: string, init?: RequestInit): Promise<T> {
    const res = await fetch(url, {
      ...init,
      headers: {
        ...this.getHeaders(),
        ...(init?.headers || {}),
      },
    });

    if (!res.ok) {
      const body = await res.text().catch(() => "");
      throw new Error(`API error ${res.status}: ${body || res.statusText}`);
    }

    return (await res.json()) as T;
  }

  private async getMissionPlanId(): Promise<number> {
    const data = await this.request<ApiMessageResponse>(`${this.API_URL}/conf/plans-count`);
    const count = parseInt(String(data.message), 10) || 0;
    if (count <= 0) throw new Error("Brak planu misji w API.");
    return count - 1;
  }

  private mapTimeline(timeline: ApiDailyState[] = []): SimulationResourcePoint[] {
    const getRes = (warehouse: ApiResource[] | undefined, target: string) => {
      const found = warehouse?.find(
        (r) => (r.type || r.resourceType || "").toUpperCase() === target,
      );
      return found?.amount ?? found?.quantity ?? 0;
    };

    return timeline.map((day) => ({
      sol: day.sol,
      energyStore: getRes(day.warehouse, "ENERGY"),
      waterStore: getRes(day.warehouse, "WATER"),
      oxygenStore: getRes(day.warehouse, "OXYGEN"),
      foodStore: getRes(day.warehouse, "FOOD"),
    }));
  }

  async generateAutomaticScenario(input: {
    missionDuration: number;
    difficulty: ScenarioDifficulty;
  }) {
    const missionPlanId = await this.getMissionPlanId();

    const draft = await this.request<ApiScenarioDraft>(`${this.API_URL}/schedule/scenario`, {
      method: "POST",
      body: JSON.stringify({
        missionPlanId: missionPlanId.toString(),
        durationSols: input.missionDuration,
        difficulty: input.difficulty,
      }),
    });

    return {
      draftId: draft.id,
      proposedEventsCount: draft.proposedEvents?.length || 0,
    };
  }

  async approveScenarioDraft(draftId: string): Promise<{ scheduleId: string }> {
    const schedule = await this.request<ApiMissionSchedule>(
      `${this.API_URL}/schedule/scenario/${draftId}/approve`,
      { method: "POST" },
    );

    return { scheduleId: schedule.id };
  }

  async createManualSchedule(input: { missionDuration: number }): Promise<{ scheduleId: string }> {
    const missionPlanId = await this.getMissionPlanId();

    const schedule = await this.request<ApiMissionSchedule>(`${this.API_URL}/schedule`, {
      method: "POST",
      body: JSON.stringify({
        missionPlanId: missionPlanId.toString(),
        durationSols: input.missionDuration,
      }),
    });

    return { scheduleId: schedule.id };
  }

  async runScenarioSimulation(input: {
    nominalSessionId: string;
    scheduleId: string;
  }): Promise<SimulationResult> {
    const data = await this.request<ApiScenariosSimulationResponse>(
      `${this.API_URL}/analysis/simulate/scenarios`,
      {
        method: "POST",
        body: JSON.stringify({
          nominalSessionId: input.nominalSessionId,
          scheduleId: input.scheduleId,
        }),
      },
    );

    return {
      sessionId: data.sessionId,
      scheduleId: input.scheduleId,
      idealTimeline: this.mapTimeline(data.idealVariant?.timeline),
      realTimeline: this.mapTimeline(data.realVariant?.timeline),
      idealOutcome: data.idealVariant?.outcome,
      realOutcome: data.realVariant?.outcome,
      appliedThreatsCount: data.appliedThreats?.length || 0,
    };
  }
}
