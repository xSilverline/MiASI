import type {
  ChartDataPoint,
  IMissionRepository,
} from "../../core/application/ports/IMissionRepository";
import type {
  MissionDashboardConfig,
  ModuleWithCount,
} from "../../core/domain/entities/MissionConfig";
import type {
  ResourceKey,
  ResourceValues,
} from "../../core/domain/entities/module";

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
      headers: this.getHeaders(), // ZMIANA: Dodajemy nagłówki
    });
    if (!res.ok) throw new Error("Nie udało się pobrać liczby planów");
    const data = await res.json();
    const count = parseInt(String(data.message), 10) || 0;
    return count > 0 ? count - 1 : 0;
  }

  private mapApiToUIConfig(apiPlan: any): Partial<MissionDashboardConfig> {
    const resources = { oxygen: 0, water: 0, food: 0 };
    apiPlan.startingResources?.forEach((r: any) => {
      if (r.resourceType === "OXYGEN") resources.oxygen = r.quantity;
      if (r.resourceType === "WATER") resources.water = r.quantity;
      if (r.resourceType === "FOOD") resources.food = r.quantity;
    });

    const modulesMap = new Map<string, any>();
    (apiPlan.modules || apiPlan.optimalModules || []).forEach((m: any) => {
      const key = m.name;
      if (modulesMap.has(key)) {
        modulesMap.get(key)!.count++;
      } else {
        const resConfig: Record<ResourceKey, ResourceValues> = {
          woda: { prod: 0, cons: 0 },
          tlen: { prod: 0, cons: 0 },
          zywnosc: { prod: 0, cons: 0 },
          energia: { prod: 0, cons: 0 },
        };
        m.type?.resourceProduction?.forEach((rp: any) => {
          if (rp.resourceType === "WATER") resConfig.woda.prod = rp.quantity;
          if (rp.resourceType === "OXYGEN") resConfig.tlen.prod = rp.quantity;
          if (rp.resourceType === "FOOD") resConfig.zywnosc.prod = rp.quantity;
          if (rp.resourceType === "ENERGY")
            resConfig.energia.prod = rp.quantity;
        });
        m.type?.resourceConsumption?.forEach((rc: any) => {
          if (rc.resourceType === "WATER") resConfig.woda.cons = rc.quantity;
          if (rc.resourceType === "OXYGEN") resConfig.tlen.cons = rc.quantity;
          if (rc.resourceType === "FOOD") resConfig.zywnosc.cons = rc.quantity;
          if (rc.resourceType === "ENERGY")
            resConfig.energia.cons = rc.quantity;
        });
        modulesMap.set(key, {
          name: m.name,
          type: m.type?.name || "NIEZNANY",
          resources: resConfig,
          count: 1,
        });
      }
    });

    return {
      resources,
      modulesList: Array.from(modulesMap.values()).map((item, index) => ({
        id: `mod-${index}`,
        ...item,
      })),
    };
  }

  private mapTimelineToChart(timeline: any[]): ChartDataPoint[] {
    return timeline.map((day) => {
      // Bezpieczne wyszukiwanie zasobu w tablicy
      const getRes = (arr: any[], type: string) =>
        arr?.find((r: any) => r.type === type)?.amount || 0;

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

  // Zaktualizowana metoda recalculate:
  async recalculate(
    payloadSessionId: string,
  ): Promise<{ nominalSessionId: string; chartData: ChartDataPoint[] }> {
    const res = await fetch(`${this.API_URL}/analysis/simulate/nominal`, {
      method: "POST",
      headers: this.getHeaders(),
      body: JSON.stringify({
        payloadSessionId,
        customizedModules: [], // W przyszłości podepniemy tu listę modyfikacji z UI
        customizedSupplies: [],
      }),
    });

    if (!res.ok) throw new Error("Błąd symulacji nominalnej");
    const data = await res.json();
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
    const plan = await res.json();

    const crew = { men: 0, women: 0 };
    const consumptionRates = {
      maleFood: { opt: 0, min: 0 },
      femaleFood: { opt: 0, min: 0 },
      oxygen: { opt: 0, min: 0 },
      water: { opt: 0, min: 0 },
    };

    plan.crew?.forEach((p: any) => {
      const isMale =
        p.name.toLowerCase().includes("male") ||
        p.name.toLowerCase().includes("męż");
      if (isMale) {
        crew.men = p.population;
        consumptionRates.maleFood.opt = p.optimalDemand?.["FOOD"] || 0;
        consumptionRates.maleFood.min = p.minimalDemand?.["FOOD"] || 0;
      } else {
        crew.women = p.population;
        consumptionRates.femaleFood.opt = p.optimalDemand?.["FOOD"] || 0;
        consumptionRates.femaleFood.min = p.minimalDemand?.["FOOD"] || 0;
      }
      if (p.optimalDemand?.["OXYGEN"])
        consumptionRates.oxygen.opt = p.optimalDemand["OXYGEN"];
      if (p.minimalDemand?.["OXYGEN"])
        consumptionRates.oxygen.min = p.minimalDemand["OXYGEN"];
      if (p.optimalDemand?.["WATER"])
        consumptionRates.water.opt = p.optimalDemand["WATER"];
      if (p.minimalDemand?.["WATER"])
        consumptionRates.water.min = p.minimalDemand["WATER"];
    });

    const mapped = this.mapApiToUIConfig(plan);

    return {
      crew,
      consumptionRates,
      resources: mapped.resources!,
      modulesList: mapped.modulesList!,
      eventsList: [],
      missionDuration: plan.missionDurationSols || 700,
    };
  }

  async saveConfig(data: Partial<MissionDashboardConfig>): Promise<void> {
    return Promise.resolve();
  }

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
    const data = await res.json();

    return {
      payloadSessionId: data.sessionId,
      updatedConfig: this.mapApiToUIConfig(data.configuration),
    };
  }
}
