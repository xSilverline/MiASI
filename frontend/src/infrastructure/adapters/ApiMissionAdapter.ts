// ======= ZMIANA ========
import type {
  ChartDataPoint,
  IMissionRepository,
} from "../../core/application/ports/IMissionRepository";
import type {
  MissionDashboardConfig,
  ModuleWithCount,
} from "../../core/domain/entities/MissionConfig";

interface ApiResource {
  type?: string;
  amount?: number;
  resourceType?: string;
  quantity?: number;
}
interface ApiEventDefinition {
  id: string;
  name?: string;
  type: "SUPPLY_DELIVERY" | "THREAT" | "MODULE_STATE_CHANGE";
  description?: string;
  effects?: {
    target: string;
    value: number;
    unit: string;
    description?: string;
  }[];
}

interface ApiCrewProfile {
  name: string;
  population: number;
  optimalDemand?: Record<string, number>;
  minimalDemand?: Record<string, number>;
}

interface ApiModule {
  name: string;
  status?: string;
  category?: string;
  weight?: number;
  resourceProduction?: ApiResource[];
  resourceConsumption?: ApiResource[];
}

interface ApiMissionPlan {
  missionDurationSols?: number;
  maxStartingWeight?: number;
  crew?: ApiCrewProfile[];
  startingResources?: ApiResource[];
  modules?: ApiModule[];
  optimalModules?: ApiModule[];
}

interface ApiDailyState {
  sol: number;
  warehouse?: ApiResource[];
  balance?: {
    produced?: ApiResource[];
    consumed?: ApiResource[];
  };
}

interface ApiSimulateNominalResponse {
  sessionId: string;
  nominalVariant?: {
    timeline?: ApiDailyState[];
  };
}

interface ApiOptimizeResponse {
  sessionId: string;
  configuration: ApiMissionPlan;
}

interface ApiMessageResponse {
  message: string;
}
// ---------------------------------------------------

export class ApiMissionAdapter implements IMissionRepository {
  private readonly API_URL = "http://localhost:8080/api";

  private getHeaders() {
    const token = localStorage.getItem("sessionToken");
    return {
      "Content-Type": "application/json",
      Authorization: token ? `Bearer ${token}` : "",
    };
  }
  private async getMissionPlanId(): Promise<number> {
    const res = await fetch(`${this.API_URL}/conf/plans-count`, {
      headers: this.getHeaders(),
    });
    if (!res.ok) throw new Error("Nie udało się pobrać liczby planów");
    const data = (await res.json()) as ApiMessageResponse;
    const count = parseInt(String(data.message), 10) || 0;
    return count > 0 ? count - 1 : -1; // Zwracamy -1, jeśli baza jest pusta
  }

  private mapApiToUIConfig(
    apiPlan: ApiMissionPlan,
  ): Partial<MissionDashboardConfig> {
    const resources = { oxygen: 0, water: 0, food: 0 };
    apiPlan.startingResources?.forEach((r) => {
      const type = (r.resourceType || r.type || "").toUpperCase();
      const val = r.quantity ?? r.amount ?? 0;

      if (type === "OXYGEN") resources.oxygen = val;
      else if (type === "WATER") resources.water = val;
      else if (type === "FOOD") resources.food = val;
    });

    const modulesMap = new Map<string, Omit<ModuleWithCount, "id">>();
    const modulesList = apiPlan.modules || apiPlan.optimalModules || [];

    modulesList.forEach((m) => {
      if (modulesMap.has(m.name)) {
        modulesMap.get(m.name)!.count++;
      } else {
        modulesMap.set(m.name, {
          name: m.name,
          category: (m.category as any) || "UTILITY_MODULE",
          status: (m.status as any) || "ACTIVE",
          weight: m.weight ?? 0,
          resourceProduction: (m.resourceProduction || []).map((rp) => ({
            resourceType: (rp.resourceType || rp.type) as any,
            quantity: rp.quantity ?? rp.amount ?? 0,
          })),
          resourceConsumption: (m.resourceConsumption || []).map((rc) => ({
            resourceType: (rc.resourceType || rc.type) as any,
            quantity: rc.quantity ?? rc.amount ?? 0,
          })),
          count: 1,
        });
      }
    });

    return {
      startingResources: [
        { resourceType: "OXYGEN", quantity: resources.oxygen },
        { resourceType: "WATER", quantity: resources.water },
        { resourceType: "FOOD", quantity: resources.food },
      ],
      modulesList: Array.from(modulesMap.values()).map((item, index) => ({
        id: `mod-${index}`,
        ...item,
      })),
    };
  }
  // 3. Podmień ciało metody saveConfig
  async saveConfig(data: Partial<MissionDashboardConfig>): Promise<void> {
    const fullData = data as MissionDashboardConfig;
    const missionId = await this.getMissionPlanId();

    // Tworzymy payload - teraz to praktycznie czyste przepisanie danych!
    const planDto = {
      missionDurationSols: fullData.missionDuration || 700,
      maxStartingWeight: fullData.maxStartingWeight || 150000,
      crew: fullData.crew,
      startingResources: fullData.startingResources,
      modules: fullData.modulesList.flatMap((m) =>
        Array.from({ length: m.count > 0 ? m.count : 1 }).map(() => ({
          name: m.name,
          status: m.status,
          category: m.category,
          weight: m.weight,
          resourceProduction: m.resourceProduction,
          resourceConsumption: m.resourceConsumption,
        })),
      ),
    };

    const url =
      missionId >= 0
        ? `${this.API_URL}/conf/plan?override=${missionId}`
        : `${this.API_URL}/conf/plan`;
    const res = await fetch(url, {
      method: "POST",
      headers: this.getHeaders(),
      body: JSON.stringify(planDto),
    });

    if (!res.ok) {
      throw new Error(`Błąd zapisu do API: ${res.status}`);
    }
  }

  private mapTimelineToChart(timeline: ApiDailyState[]): ChartDataPoint[] {
    return timeline.map((day) => {
      // Pancerne pobieranie zasobu niezależnie od wersji API
      const getRes = (arr: ApiResource[] | undefined, targetType: string) => {
        const found = arr?.find(
          (r) => r.type === targetType || r.resourceType === targetType,
        );
        if (!found) return 0;
        return found.amount !== undefined ? found.amount : found.quantity || 0;
      };

      return {
        sol: day.sol,
        energyStore: getRes(day.warehouse, "ENERGY"),
        energyProd: getRes(day.balance?.produced, "ENERGY"),
        energyCons: getRes(day.balance?.consumed, "ENERGY"),
        waterStore: getRes(day.warehouse, "WATER"),
        waterProd: getRes(day.balance?.produced, "WATER"),
        waterCons: getRes(day.balance?.consumed, "WATER"),
        oxygenStore: getRes(day.warehouse, "OXYGEN"),
        oxygenProd: getRes(day.balance?.produced, "OXYGEN"),
        oxygenCons: getRes(day.balance?.consumed, "OXYGEN"),
        foodStore: getRes(day.warehouse, "FOOD"),
        foodProd: getRes(day.balance?.produced, "FOOD"),
        foodCons: getRes(day.balance?.consumed, "FOOD"),
      };
    });
  }

  async recalculate(
    payloadSessionId: string,
  ): Promise<{ nominalSessionId: string; chartData: ChartDataPoint[] }> {
    const res = await fetch(`${this.API_URL}/analysis/simulate/nominal`, {
      method: "POST",
      headers: this.getHeaders(),
      body: JSON.stringify({
        payloadSessionId,
        customizedModules: [],
        customizedSupplies: [],
      }),
    });

    if (!res.ok) throw new Error("Błąd symulacji nominalnej");
    const data = (await res.json()) as ApiSimulateNominalResponse;
    const chartData = this.mapTimelineToChart(
      data.nominalVariant?.timeline || [],
    );

    return {
      nominalSessionId: data.sessionId,
      chartData,
    };
  }

  async getConfig(): Promise<MissionDashboardConfig | null> {
    const missionId = await this.getMissionPlanId();
    if (missionId === -1) return null;

    const res = await fetch(`${this.API_URL}/conf/${missionId}/plan`, {
      headers: this.getHeaders(),
    });
    if (!res.ok) throw new Error("Błąd pobierania planu");
    const plan = (await res.json()) as ApiMissionPlan;

    // Przekazanie gotowych załóg z API z drobnym fallbackiem
    const crew = (plan.crew || []).map((c) => ({
      name: c.name,
      population: c.population || 0,
      optimalDemand: c.optimalDemand || {},
      minimalDemand: c.minimalDemand || {},
    }));

    const mapped = this.mapApiToUIConfig(plan);

    const eventsRes = await fetch(`${this.API_URL}/event-catalog`, {
      headers: this.getHeaders(),
    });

    const eventsCatalog = eventsRes.ok
      ? ((await eventsRes.json()) as ApiEventDefinition[])
      : [];

    return {
      crew,
      startingResources: mapped.startingResources || [],
      modulesList: mapped.modulesList || [],
      eventsList: eventsCatalog.map((event) => ({
        id: event.id,
        name: event.name,
        type: event.type,
        description: event.description,
        effects: event.effects || [],
      })),
      missionDuration: plan.missionDurationSols || 700,
      maxStartingWeight: plan.maxStartingWeight || 150000,
    };
  }

  // async saveConfig(data: Partial<MissionDashboardConfig>): Promise<void> {
  //   // Rzutujemy bezpiecznie na pełen typ dla ułatwienia odczytu (w logice upewniliśmy się, że jest pełny)
  //   const fullData = data as MissionDashboardConfig;
  //
  //   const missionId = await this.getMissionPlanId();
  //
  //   const planDto = {
  //     missionDurationSols: fullData.missionDuration,
  //     maxStartingWeight: fullData.maxStartingWeight || 150000,
  //     crew: [
  //       {
  //         name: "Male",
  //         population: fullData.crew.men,
  //         optimalDemand: {
  //           FOOD: fullData.consumptionRates.maleFood.opt,
  //           WATER: fullData.consumptionRates.water.opt,
  //           OXYGEN: fullData.consumptionRates.oxygen.opt,
  //         },
  //         minimalDemand: {
  //           FOOD: fullData.consumptionRates.maleFood.min,
  //           WATER: fullData.consumptionRates.water.min,
  //           OXYGEN: fullData.consumptionRates.oxygen.min,
  //         },
  //       },
  //       {
  //         name: "Female",
  //         population: fullData.crew.women,
  //         optimalDemand: {
  //           FOOD: fullData.consumptionRates.femaleFood.opt,
  //           WATER: fullData.consumptionRates.water.opt,
  //           OXYGEN: fullData.consumptionRates.oxygen.opt,
  //         },
  //         minimalDemand: {
  //           FOOD: fullData.consumptionRates.femaleFood.min,
  //           WATER: fullData.consumptionRates.water.min,
  //           OXYGEN: fullData.consumptionRates.oxygen.min,
  //         },
  //       },
  //     ],
  //     startingResources: [
  //       { resourceType: "OXYGEN", quantity: fullData.resources.oxygen },
  //       { resourceType: "WATER", quantity: fullData.resources.water },
  //       { resourceType: "FOOD", quantity: fullData.resources.food },
  //     ],
  //     modules: fullData.modulesList.flatMap((m) => {
  //       // Tłumaczenie potencjalnych starych/błędnych typów z UI na ścisły Enum API
  //       let safeCategory = "UTILITY_MODULE";
  //       if (m.type === "ENERGY_MODULE" || m.type === "energetyczny") {
  //         safeCategory = "ENERGY_MODULE";
  //       }
  //
  //       return Array.from({ length: m.count > 0 ? m.count : 1 }).map(() => ({
  //         name: m.name,
  //         status: m.status || "ACTIVE",
  //         category: safeCategory, // ZMIANA: Wysyłamy wyłącznie przefiltrowany Enum
  //         weight: m.weight || 0,
  //         resourceProduction: [
  //           { resourceType: "WATER", quantity: m.resources.woda.prod },
  //           { resourceType: "OXYGEN", quantity: m.resources.tlen.prod },
  //           { resourceType: "FOOD", quantity: m.resources.zywnosc.prod },
  //           { resourceType: "ENERGY", quantity: m.resources.energia.prod },
  //         ],
  //         resourceConsumption: [
  //           { resourceType: "WATER", quantity: m.resources.woda.cons },
  //           { resourceType: "OXYGEN", quantity: m.resources.tlen.cons },
  //           { resourceType: "FOOD", quantity: m.resources.zywnosc.cons },
  //           { resourceType: "ENERGY", quantity: m.resources.energia.cons },
  //         ],
  //       }));
  //     }),
  //   };
  //
  //   const url =
  //     missionId >= 0
  //       ? `${this.API_URL}/conf/plan?override=${missionId}`
  //       : `${this.API_URL}/conf/plan`;
  //
  //   const res = await fetch(url, {
  //     method: "POST",
  //     headers: this.getHeaders(),
  //     body: JSON.stringify(planDto),
  //   });
  //
  //   if (!res.ok) {
  //     throw new Error(`Błąd podczas zapisu do API: ${res.status}`);
  //   }
  // }

  async optimize(): Promise<{
    payloadSessionId: string;
    updatedConfig: Partial<MissionDashboardConfig>;
  }> {
    const missionPlanId = await this.getMissionPlanId();

    const res = await fetch(`${this.API_URL}/analysis/payload/optimize`, {
      method: "POST",
      headers: this.getHeaders(),
      body: JSON.stringify({ missionPlanId: missionPlanId.toString() }),
    });

    if (!res.ok) throw new Error("Błąd auto-optymalizacji");
    const data = (await res.json()) as ApiOptimizeResponse;

    return {
      payloadSessionId: data.sessionId,
      updatedConfig: this.mapApiToUIConfig(data.configuration),
    };
  }
}
// ========= KONIEC SEKCJI========
