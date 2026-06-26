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

/**
 * UI model for an event placed on the mission timeline.
 *
 * `eventId` is the catalog definition id when the event was created from the
 * frontend. Some timeline endpoints return only the scheduled event id, so the
 * adapter also carries optional display fields (`name`, `type`, etc.) to let the
 * schedule render events even when the catalog definition cannot be matched.
 */
export interface ScheduledEvent {
  id: string;
  eventId: string;
  startDay: number;
  duration: number;
  name?: string;
  type?: EventType;
  description?: string;
  effects?: EventEffect[];
}
