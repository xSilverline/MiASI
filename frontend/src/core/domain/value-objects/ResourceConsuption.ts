import type { ResourceType } from "../entities/module.ts";

export interface SexProfile {
  name: string;
  population: number;
  optimalDemand: Partial<Record<ResourceType, number>>;
  minimalDemand: Partial<Record<ResourceType, number>>;
}
