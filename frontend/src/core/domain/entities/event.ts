export type EventType = "SUPPLY_DELIVERY" | "THREAT" | "MODULE_STATE_CHANGE";

export interface EventEffect {
  target: string;
  value: number;
  unit: string;
  description?: string;
}

export interface EventData {
  id: string;
  name?: string;
  type: EventType;
  description?: string;
  sol?: number;
  duration?: number;
  effects: EventEffect[];
}

export interface ScheduledEvent {
  id: string;
  eventId: string;
  startDay: number;
  duration: number;
}


