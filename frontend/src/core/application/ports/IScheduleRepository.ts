import type { ScheduledEvent } from "../../domain/entities/event";

export interface IScheduleRepository {
  getEvents(): Promise<ScheduledEvent[]>;
  saveEvent(event: ScheduledEvent): Promise<void>;
  deleteEvent(id: string): Promise<void>;
}
