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

export const MOCK_MODULES: ModuleData[] = [
  {
    id: "mod-1",
    name: "Farma Hydroponiczna",
    type: "produkcyjny",
    resources: {
      woda: { prod: 0, cons: 15 },
      tlen: { prod: 25, cons: 0 },
      zywnosc: { prod: 40, cons: 0 },
      energia: { prod: 0, cons: 35 },
    },
  },
  {
    id: "mod-2",
    name: "Kwatery Główne",
    type: "mieszkalny",
    resources: {
      woda: { prod: 0, cons: 5 },
      tlen: { prod: 0, cons: 10 },
      zywnosc: { prod: 0, cons: 10 },
      energia: { prod: 0, cons: 20 },
    },
  },
  {
    id: "mod-3",
    name: "Reaktor Jądrowy",
    type: "energetyczny",
    resources: {
      woda: { prod: 0, cons: 50 },
      tlen: { prod: 0, cons: 0 },
      zywnosc: { prod: 0, cons: 0 },
      energia: { prod: 1000, cons: 0 },
    },
  },
];
