import type { MissionDashboardConfig } from "../../domain/entities/MissionConfig";

export interface ChartDataPoint {
  sol: number;
  energyStore: number;
  energyProd: number;
  energyCons: number;
  waterStore: number;
  waterProd: number;
  waterCons: number;
  oxygenStore: number;
  oxygenProd: number;
  oxygenCons: number;
  foodStore: number;
  foodProd: number;
  foodCons: number;
}

export interface IMissionRepository {
  getConfig(): Promise<MissionDashboardConfig | null>;
  saveConfig(data: Partial<MissionDashboardConfig>): Promise<void>;

  optimize(): Promise<{
    payloadSessionId: string;
    updatedConfig: Partial<MissionDashboardConfig>;
  }>;

  recalculate(
    payloadSessionId: string,
  ): Promise<{ nominalSessionId: string; chartData: ChartDataPoint[] }>;
}
