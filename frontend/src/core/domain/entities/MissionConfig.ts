import type { ModuleData, ResourceQuantity } from "./module.ts";
import type { EventData } from "./event.ts";
import type { SexProfile } from "../value-objects/ResourceConsuption.ts";

export type ModuleWithCount = ModuleData & { count: number };

export interface MissionDashboardConfig {
  missionDuration: number;
  maxStartingWeight: number;
  crew: SexProfile[];
  startingResources: ResourceQuantity[];
  modulesList: ModuleWithCount[];
  eventsList: EventData[];
}
