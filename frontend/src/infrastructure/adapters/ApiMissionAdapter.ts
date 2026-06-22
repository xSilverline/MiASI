import type { IMissionRepository } from "../../core/application/ports/IMissionRepository";
import type { MissionPlan } from "../../core/domain/entities/MissionPlan";

export class ApiMissionAdapter implements IMissionRepository {
  private readonly API_URL = "/api";

  async getConfig(): Promise<MissionPlan> {
    try {
      const countResponse = await fetch(`${this.API_URL}/conf/plans-count`);
      if (!countResponse.ok) throw new Error("Błąd pobierania ilości planów");
      const countData = await countResponse.json();
      const plansCount = parseInt(String(countData.message), 10) || 0;

      let planResponse;

      if (plansCount === 0) {
        planResponse = await fetch(`${this.API_URL}/conf/default/plan`);
      } else {
        planResponse = await fetch(`${this.API_URL}/conf/0/plan`);
      }

      if (!planResponse.ok) {
        throw new Error("Nie udało się pobrać planu misji z serwera");
      }

      return (await planResponse.json()) as MissionPlan;
    } catch (error) {
      console.error("Błąd komunikacji z API Mission Plan:", error);
      throw error;
    }
  }

  async saveConfig(data: Partial<MissionPlan>): Promise<void> {
    await fetch(`${this.API_URL}/conf/plan?override=0`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(data),
    });
  }

  async recalculate(): Promise<void> {
    return Promise.resolve();
  }
}
