export type ResourceType = "FOOD" | "OXYGEN" | "WATER" | "ENERGY";
export type ModuleStatus =
  | "ACTIVE"
  | "PARTIALLY_DAMAGED"
  | "DESTROYED"
  | "INACTIVE";

export interface Resources {
  resourceType: ResourceType;
  quantity: number;
}

export interface ModuleType {
  name: string;
  resourceConsumption: Resources[];
  resourceProduction: Resources[];
}

export interface Module {
  name: string;
  status: ModuleStatus;
  type: ModuleType;
  weight: number;
}

export interface SexProfile {
  name: string;
  population: number;
  optimalDemand: Record<string, number>;
  minimalDemand: Record<string, number>;
}

export interface MissionPlan {
  crew: SexProfile[];
  missionDurationSols: number;
  startingResources: Resources[];
  modules: Module[];
  maxStartingWeight: number;
}
