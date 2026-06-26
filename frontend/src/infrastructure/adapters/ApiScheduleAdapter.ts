import type { IScheduleRepository } from "../../core/application/ports/IScheduleRepository";
import type { ScheduledEvent } from "../../core/domain/entities/event";

interface ApiEventEffect {
  target: string;
  value: number;
  unit: string;
  description?: string;
}

interface ApiScheduledEvent {
  id: string;
  type?: string;
  sol?: number;
  description?: string;
  effects?: ApiEventEffect[];
}

interface ApiTimelineSolResponse {
  sol: number;
  events: ApiScheduledEvent[];
}

export class ApiScheduleAdapter implements IScheduleRepository {
  private readonly API_URL = "http://localhost:8080/api";

  private getHeaders() {
    const token = localStorage.getItem("sessionToken");

    return {
      "Content-Type": "application/json",
      Authorization: token ? `Bearer ${token}` : "",
    };
  }

  private async request<T>(url: string, init?: RequestInit): Promise<T> {
    const res = await fetch(url, {
      ...init,
      headers: {
        ...this.getHeaders(),
        ...(init?.headers || {}),
      },
    });

    if (!res.ok) {
      const body = await res.text().catch(() => "");
      throw new Error(`API error ${res.status}: ${body || res.statusText}`);
    }

    if (res.status === 204) {
      return undefined as T;
    }

    return (await res.json()) as T;
  }

  private mapTimelineToScheduledEvents(
    timeline: ApiTimelineSolResponse[],
  ): ScheduledEvent[] {
    return timeline.flatMap((day) =>
      (day.events || []).map((event) => ({
        id: event.id,
        eventId: event.id,
        startDay: event.sol ?? day.sol,
        duration: 1,
      })),
    );
  }

  async getEvents(): Promise<ScheduledEvent[]> {
    const timeline = await this.request<ApiTimelineSolResponse[]>(
      `${this.API_URL}/timeline`,
    );

    return this.mapTimelineToScheduledEvents(timeline);
  }

  async saveEvent(event: ScheduledEvent): Promise<void> {
    await this.request<ApiScheduledEvent>(`${this.API_URL}/timeline/events`, {
      method: "POST",
      body: JSON.stringify({
        sol: event.startDay,
        eventDefinitionId: event.eventId,
      }),
    });
  }

  async deleteEvent(id: string): Promise<void> {
    await this.request<void>(`${this.API_URL}/timeline/events/${id}`, {
      method: "DELETE",
    });
  }
}
