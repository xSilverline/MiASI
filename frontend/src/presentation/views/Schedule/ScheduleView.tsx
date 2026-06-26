import React, { useState } from "react";
import { AlertTriangle, Loader2 } from "lucide-react";
import { DayScheduleModal } from "./DayScheduleModal.tsx";

import type {
  EventData,
  ScheduledEvent,
} from "../../../core/domain/entities/event.ts";
import { useScheduleData } from "../../hooks/useScheduleData.ts";

interface ScheduleViewProps {
  missionDuration: number;
  availableEvents: EventData[];
}

type ScheduledEventWithDetails = ScheduledEvent & { eventDetails: EventData };

export const ScheduleView: React.FC<ScheduleViewProps> = ({
  missionDuration,
  availableEvents,
}) => {
  const days = Array.from({ length: missionDuration }, (_, i) => i + 1);
  const [selectedDay, setSelectedDay] = useState<number | null>(null);
  const { scheduledEvents, isLoading, error, saveEvent, deleteEvent } =
    useScheduleData();

  const getFallbackDetails = (event: ScheduledEvent): EventData => ({
    id: event.eventId || event.id,
    name:
      event.name ||
      event.description ||
      (event.type === "THREAT"
        ? "Zagrożenie"
        : event.type === "SUPPLY_DELIVERY"
          ? "Dostawa"
          : event.type === "MODULE_STATE_CHANGE"
            ? "Zmiana stanu modułu"
            : "Zdarzenie"),
    type: event.type || "THREAT",
    description: event.description,
    sol: event.startDay,
    duration: event.duration,
    effects: event.effects || [],
  });

  const getEventDetails = (event: ScheduledEvent): EventData =>
    availableEvents.find((catalogEvent) => catalogEvent.id === event.eventId) ||
    getFallbackDetails(event);

  const getEventsForDay = (day: number): ScheduledEventWithDetails[] => {
    return scheduledEvents
      .filter(
        (event) =>
          day >= event.startDay && day < event.startDay + event.duration,
      )
      .map((event) => ({
        ...event,
        eventDetails: getEventDetails(event),
      }));
  };

  return (
    <div className="flex flex-col h-full bg-mars-itemBackground rounded-2xl p-8 border border-mars-orange/10 shadow-lg relative">
      <div className="flex justify-between items-center mb-6 shrink-0">
        <div>
          <h2 className="text-xl font-bold tracking-widest text-white uppercase">
            Harmonogram Misji
          </h2>
          <p className="text-xs tracking-widest uppercase text-slate-500 mt-2">
            {scheduledEvents.length} zdarzeń na osi czasu
          </p>
        </div>

        {isLoading && (
          <div className="flex items-center gap-2 text-mars-orange text-xs tracking-widest uppercase">
            <Loader2 size={16} className="animate-spin" /> Ładowanie timeline
          </div>
        )}
      </div>

      {error && (
        <div className="mb-4 flex items-center gap-3 rounded-xl border border-red-500/30 bg-red-500/10 px-4 py-3 text-red-400 text-xs tracking-widest uppercase">
          <AlertTriangle size={18} /> {error}
        </div>
      )}

      <div className="flex-1 overflow-y-auto pr-2 custom-scrollbar rounded-lg border border-mars-line/50 bg-mars-background relative">
        <div className="grid grid-cols-10 gap-px bg-mars-line/50">
          {days.map((day) => {
            const dayEvents = getEventsForDay(day);

            return (
              <div
                key={day}
                onClick={() => setSelectedDay(day)}
                className="bg-mars-background min-h-[100px] p-3 flex flex-col hover:bg-mars-orange/5 transition-colors cursor-pointer group relative"
              >
                <span className="text-mars-orange font-bold text-sm group-hover:text-white transition-colors">
                  {day}
                </span>

                <div className="mt-2 flex flex-col gap-1 flex-1 overflow-hidden">
                  {dayEvents.map((event) => (
                    <span
                      key={event.id}
                      title={event.eventDetails.name}
                      className={`truncate text-[10px] px-2 py-0.5 rounded-full font-semibold border ${
                        event.eventDetails.type === "THREAT"
                          ? "bg-red-600/20 text-red-500 border-red-600/50"
                          : "bg-green-600/20 text-green-500 border-green-600/50"
                      }`}
                    >
                      {event.eventDetails.name}
                    </span>
                  ))}
                </div>
              </div>
            );
          })}
        </div>
      </div>

      {selectedDay !== null && (
        <DayScheduleModal
          day={selectedDay}
          availableEvents={availableEvents}
          dayEvents={getEventsForDay(selectedDay)}
          onClose={() => setSelectedDay(null)}
          onSave={saveEvent}
          onDelete={deleteEvent}
        />
      )}
    </div>
  );
};
