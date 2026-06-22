import { type ModuleData } from "./module.ts";
import { type EventData } from "./event.ts";
import type { ResourceConsumption } from "../value-objects/ResourceConsuption.ts";

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
