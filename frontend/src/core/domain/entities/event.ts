export type TargetType = "module" | "resource";
export type ActionType = "efficiency" | "plus" | "minus";

export interface EventImpact {
  id: string;
  targetId: string;
  targetName: string;
  targetType: TargetType;
  actionType: ActionType;
  value: number;
}

export interface EventData {
  id: string;
  name: string;
  type: string;
  duration: number;
  impacts: EventImpact[];
}

export interface ScheduledEvent {
  id: string; // Unikalne ID instancji w harmonogramie
  eventId: string; // ID zdarzenia z bazy (np. 'ev-1')
  startDay: number;
  duration: number;
}
