import type { MissionPlan } from "../../domain/entities/MissionPlan";

export interface IMissionRepository {
  getConfig(): Promise<MissionPlan>;
  saveConfig(data: Partial<MissionPlan>): Promise<void>;
  recalculate(): Promise<void>;
}
