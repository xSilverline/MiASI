import type { IScheduleRepository } from "../../core/application/ports/IScheduleRepository";
import type {
  EventEffect,
  EventType,
  ScheduledEvent,
} from "../../core/domain/entities/event";

interface ApiScheduledEvent {
  id?: string;
  eventId?: string;
  eventDefinitionId?: string;
  name?: string;
  type?: EventType;
  sol?: number;
  startDay?: number;
  duration?: number;
  durationSols?: number;
  description?: string;
  effects?: EventEffect[];
}

interface ApiTimelineSolResponse {
  sol: number;
  events?: ApiScheduledEvent[];
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

    if (res.status === 204) return undefined as T;

    const text = await res.text();
    if (!text) return undefined as T;

    return JSON.parse(text) as T;
  }

  private mapApiEventToScheduledEvent(
    event: ApiScheduledEvent,
    fallbackSol: number,
  ): ScheduledEvent {
    const id =
      event.id ||
      event.eventId ||
      event.eventDefinitionId ||
      crypto.randomUUID();
    const eventId = event.eventDefinitionId || event.eventId || event.id || id;

    return {
      id,
      eventId,
      startDay: event.sol ?? event.startDay ?? fallbackSol,
      duration: event.duration ?? event.durationSols ?? 1,
      name: event.name,
      type: event.type,
      description: event.description,
      effects: event.effects || [],
    };
  }

  private mapTimelineToScheduledEvents(
    timeline: ApiTimelineSolResponse[],
  ): ScheduledEvent[] {
    return timeline.flatMap((day) =>
      (day.events || []).map((event) =>
        this.mapApiEventToScheduledEvent(event, day.sol),
      ),
    );
  }

  async getEvents(): Promise<ScheduledEvent[]> {
    const timeline = await this.request<ApiTimelineSolResponse[]>(
      `${this.API_URL}/timeline`,
    );

    return this.mapTimelineToScheduledEvents(
      Array.isArray(timeline) ? timeline : [],
    );
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
