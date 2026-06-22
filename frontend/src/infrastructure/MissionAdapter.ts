import { type ModuleData, MOCK_MODULES } from "../types/module";
import { type EventData, MOCK_EVENTS } from "../types/events";
import type { ResourceConsumption } from "../views/ConfigCreator/ResourceConfigView";
import {
  MISSION_DURATION,
  CREW_MEMBERS_NUMBER,
  OXYGEN_AMOUNT,
  WATER_AMOUNT,
  FOOD_AMOUNT,
} from "../config/config";

export interface CrewData {
  men: number;
  women: number;
}

export interface ResourcesData {
  oxygen: number;
  water: number;
  food: number;
}

export type ModuleWithCount = ModuleData & { count: number };

export interface MissionDashboardConfig {
  crew: CrewData;
  resources: ResourcesData;
  consumptionRates: ResourceConsumption;
  modulesList: ModuleWithCount[];
  eventsList: EventData[];
  missionDuration: number;
}

export interface IMissionRepository {
  getConfig(): Promise<MissionDashboardConfig>;
  saveConfig(data: Partial<MissionDashboardConfig>): Promise<void>;
  recalculate(): Promise<void>;
}

export class MockMissionAdapter implements IMissionRepository {
  private db: MissionDashboardConfig = {
    crew: {
      men: Math.floor(CREW_MEMBERS_NUMBER / 2),
      women: Math.ceil(CREW_MEMBERS_NUMBER / 2),
    },
    resources: {
      oxygen: OXYGEN_AMOUNT,
      water: WATER_AMOUNT,
      food: FOOD_AMOUNT,
    },
    consumptionRates: {
      maleFood: { opt: 0, min: 0 },
      femaleFood: { opt: 0, min: 0 },
      oxygen: { opt: 0, min: 0 },
      water: { opt: 0, min: 0 },
    },

    modulesList: MOCK_MODULES.map((mod) => ({
      ...mod,
      count: 1,
    })) as ModuleWithCount[],
    eventsList: MOCK_EVENTS,
    missionDuration: MISSION_DURATION,
  };

  async getConfig(): Promise<MissionDashboardConfig> {
    return new Promise((resolve) =>
      setTimeout(() => resolve({ ...this.db }), 500),
    );
  }

  async saveConfig(data: Partial<MissionDashboardConfig>): Promise<void> {
    return new Promise((resolve) => {
      setTimeout(() => {
        this.db = { ...this.db, ...data };
        resolve();
      }, 500);
    });
  }

  async recalculate(): Promise<void> {
    return new Promise((resolve) => setTimeout(resolve, 1500));
  }
}

export const missionAdapter = new MockMissionAdapter();
