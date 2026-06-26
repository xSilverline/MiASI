export type ResourceType = "FOOD" | "OXYGEN" | "WATER" | "ENERGY";
export type ModuleStatus =
  | "ACTIVE"
  | "PARTIALLY_DAMAGED"
  | "DESTROYED"
  | "INACTIVE";
export type ModuleCategory = "UTILITY_MODULE" | "ENERGY_MODULE";

export interface ResourceQuantity {
  resourceType: ResourceType;
  quantity: number;
}

export interface ModuleData {
  id: string;
  name: string;
  status: ModuleStatus;
  category: ModuleCategory;
  weight: number;
  resourceProduction: ResourceQuantity[];
  resourceConsumption: ResourceQuantity[];
}
