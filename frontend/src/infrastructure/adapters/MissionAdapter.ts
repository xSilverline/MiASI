import {
  MISSION_DURATION,
  CREW_MEMBERS_NUMBER,
  OXYGEN_AMOUNT,
  WATER_AMOUNT,
  FOOD_AMOUNT,
} from "../mock-data/config.ts";
import type { IMissionRepository } from "../../core/application/ports/IMissionRepository";
import type {
  MissionDashboardConfig,
  ModuleWithCount,
} from "../../core/domain/entities/MissionConfig";
import { MOCK_MODULES } from "../mock-data/moduleData.ts";
import { MOCK_EVENTS } from "../mock-data/eventsData.ts";

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
