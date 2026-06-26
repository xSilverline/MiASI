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

interface ApiEventDefinition {
  id?: string;
  name?: string;
  type?: "SUPPLY_DELIVERY" | "THREAT" | "MODULE_STATE_CHANGE";
  description?: string;
  affectedElement?: string;
  consequence?: string;
  effects?: {
    target: string;
    value: number;
    unit: string;
    description?: string;
  }[];
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

  private async request<T>(
    url: string,
    init?: RequestInit,
    timeoutMs = 15000,
  ): Promise<T> {
    const controller = new AbortController();
    const timeoutId = window.setTimeout(() => controller.abort(), timeoutMs);

    try {
      const res = await fetch(url, {
        ...init,
        signal: controller.signal,
        headers: {
          ...this.getHeaders(),
          ...(init?.headers || {}),
        },
      });

      if (!res.ok) {
        const body = await res.text().catch(() => "");
        throw new Error(`API error ${res.status}: ${body || res.statusText}`);
      }

      if (res.status === 204) return undefined as T;
      return (await res.json()) as T;
    } catch (error) {
      if (error instanceof DOMException && error.name === "AbortError") {
        throw new Error(`Przekroczono limit czasu zapytania: ${url}`);
      }
      throw error;
    } finally {
      window.clearTimeout(timeoutId);
    }
  }

  private mapEventCatalogToUI(events: ApiEventDefinition[]) {
    return events
      .filter((event) => event.id && event.type)
      .map((event) => ({
        id: event.id!,
        name: event.name || event.description || event.id!,
        type: event.type!,
        description: event.description,
        effects: event.effects || [],
      }));
  }

  private mapUIEventToCatalog(event: MissionDashboardConfig["eventsList"][number]) {
    return {
      id: event.id,
      name: event.name || event.id,
      type: event.type,
      description: event.description || event.name || event.id,
      affectedElement: event.effects?.[0]?.target || "",
      consequence: event.effects?.[0]?.description || "",
      effects: event.effects || [],
    } satisfies ApiEventDefinition;
  }

  private async getEventCatalog() {
    try {
      const events = await this.request<ApiEventDefinition[]>(
        `${this.API_URL}/event-catalog`,
      );
      return this.mapEventCatalogToUI(events);
    } catch (error) {
      console.error("Błąd pobierania katalogu zdarzeń", error);
      return [];
    }
  }

  private async upsertEventCatalog(events: MissionDashboardConfig["eventsList"] | undefined) {
    if (!events || events.length === 0) return;

    const existing = await this.request<ApiEventDefinition[]>(
      `${this.API_URL}/event-catalog`,
    ).catch(() => []);
    const existingIds = new Set(existing.map((event) => event.id).filter(Boolean));

    await Promise.all(
      events.map((event) => {
        const payload = this.mapUIEventToCatalog(event);

        if (existingIds.has(event.id)) {
          return this.request<ApiEventDefinition>(
            `${this.API_URL}/event-catalog/${encodeURIComponent(event.id)}`,
            {
              method: "PUT",
              body: JSON.stringify(payload),
            },
          );
        }

        return this.request<ApiEventDefinition>(`${this.API_URL}/event-catalog`, {
          method: "POST",
          body: JSON.stringify(payload),
        });
      }),
    );
  }
  private async getMissionPlanId(): Promise<number> {
    const data = await this.request<ApiMessageResponse>(
      `${this.API_URL}/conf/plans-count`,
    );
    const count = parseInt(String(data.message), 10) || 0;
    return count > 0 ? count - 1 : -1;
  }

  private toStableModuleId(name: string): string {
    return `mod-${name
      .trim()
      .toLowerCase()
      .replace(/\s+/g, "-")
      .replace(/[^a-z0-9-]/g, "")}`;
  }

  private mapApiToUIConfig(
    apiPlan: ApiMissionPlan,
  ): Partial<MissionDashboardConfig> {
    const resources = { oxygen: 0, water: 0, food: 0, energy: 0 };
    apiPlan.startingResources?.forEach((r) => {
      const type = (r.resourceType || r.type || "").toUpperCase();
      const val = r.quantity ?? r.amount ?? 0;

      if (type === "OXYGEN") resources.oxygen = val;
      else if (type === "WATER") resources.water = val;
      else if (type === "FOOD") resources.food = val;
      else if (type === "ENERGY") resources.energy = val;
    });

    const modulesMap = new Map<string, Omit<ModuleWithCount, "id">>();
    const modulesList = apiPlan.optimalModules || apiPlan.modules || [];

    modulesList.forEach((m) => {
      const moduleName = m.name?.trim();
      if (!moduleName) return;

      if (modulesMap.has(moduleName)) {
        modulesMap.get(moduleName)!.count++;
      } else {
        modulesMap.set(moduleName, {
          name: moduleName,
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
        { resourceType: "ENERGY", quantity: resources.energy },
      ],
      modulesList: Array.from(modulesMap.values()).map((item) => ({
        id: this.toStableModuleId(item.name),
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
    await this.request<ApiMessageResponse>(url, {
      method: "POST",
      body: JSON.stringify(planDto),
    });

    await this.upsertEventCatalog(fullData.eventsList).catch((error) => {
      console.error("Błąd synchronizacji katalogu zdarzeń", error);
    });
  }

  private mapTimelineToChart(timeline: ApiDailyState[]): ChartDataPoint[] {
    const getRes = (arr: ApiResource[] | undefined, targetType: string) => {
      const found = arr?.find((r) => {
        const type = (r.type || r.resourceType || "").toUpperCase();
        return type === targetType;
      });

      if (!found) return 0;
      return found.amount !== undefined ? found.amount : found.quantity || 0;
    };

    /**
     * Dashboard chart uses absolute warehouse values returned by the API.
     * Scaling and safe-threshold visualization are handled in UsageChart.
     */
    return timeline.map((day) => ({
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
    }));
  }

  async recalculate(
    payloadSessionId: string,
  ): Promise<{ nominalSessionId: string; chartData: ChartDataPoint[] }> {
    const data = await this.request<ApiSimulateNominalResponse>(
      `${this.API_URL}/analysis/simulate/nominal`,
      {
        method: "POST",
        body: JSON.stringify({
          payloadSessionId,
          customizedModules: [],
          customizedSupplies: [],
        }),
      },
      60000,
    );
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

    const plan = await this.request<ApiMissionPlan>(
      `${this.API_URL}/conf/${missionId}/plan`,
    );

    // Przekazanie gotowych załóg z API z drobnym fallbackiem
    const crew = (plan.crew || []).map((c) => ({
      name: c.name,
      population: c.population || 0,
      optimalDemand: c.optimalDemand || {},
      minimalDemand: c.minimalDemand || {},
    }));

    const mapped = this.mapApiToUIConfig(plan);
    const eventsList = await this.getEventCatalog();

    return {
      crew,
      startingResources: mapped.startingResources || [],
      modulesList: mapped.modulesList || [],
      eventsList,
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

    const data = await this.request<ApiOptimizeResponse>(
      `${this.API_URL}/analysis/payload/optimize`,
      {
        method: "POST",
        body: JSON.stringify({ missionPlanId: missionPlanId.toString() }),
      },
      60000,
    );

    return {
      payloadSessionId: data.sessionId,
      updatedConfig: this.mapApiToUIConfig(data.configuration),
    };
  }
}
// ========= KONIEC SEKCJI========
