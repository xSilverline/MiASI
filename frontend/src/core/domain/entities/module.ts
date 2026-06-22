// src/types/module.ts

export type ResourceKey = "woda" | "tlen" | "zywnosc" | "energia";

export interface ResourceValues {
  prod: number;
  cons: number;
}

export interface ModuleData {
  id: string;
  name: string;
  type: string;
  resources: Record<ResourceKey, ResourceValues>;
}


