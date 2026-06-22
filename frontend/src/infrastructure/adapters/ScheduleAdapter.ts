import type { IScheduleRepository } from "../../core/application/ports/IScheduleRepository";
import type { ScheduledEvent } from "../../core/domain/entities/event";

export class MockScheduleAdapter implements IScheduleRepository {
  // Symulacja bazy danych dla zdarzeń
  private events: ScheduledEvent[] = [];

  async getEvents(): Promise<ScheduledEvent[]> {
    return new Promise((resolve) =>
      setTimeout(() => resolve([...this.events]), 300),
    );
  }

  async saveEvent(event: ScheduledEvent): Promise<void> {
    return new Promise((resolve) => {
      setTimeout(() => {
        const index = this.events.findIndex((e) => e.id === event.id);
        if (index >= 0) {
          // Aktualizacja istniejącego
          this.events[index] = event;
        } else {
          // Dodanie nowego
          this.events.push(event);
        }
        resolve();
      }, 300);
    });
  }

  async deleteEvent(id: string): Promise<void> {
    return new Promise((resolve) => {
      setTimeout(() => {
        this.events = this.events.filter((e) => e.id !== id);
        resolve();
      }, 300);
    });
  }
}
