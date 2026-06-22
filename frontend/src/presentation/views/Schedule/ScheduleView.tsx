import React, { useState } from "react";
import { DayScheduleModal } from "./DayScheduleModal.tsx";

import type { EventData } from "../../../core/domain/entities/event.ts";
import { useScheduleData } from "../../hooks/useScheduleData.ts";

interface ScheduleViewProps {
  missionDuration: number;
  availableEvents: EventData[];
}

export const ScheduleView: React.FC<ScheduleViewProps> = ({
  missionDuration,
  availableEvents,
}) => {
  const days = Array.from({ length: missionDuration }, (_, i) => i + 1);

  // Stan modala
  const [selectedDay, setSelectedDay] = useState<number | null>(null);
  const { scheduledEvents, saveEvent, deleteEvent } = useScheduleData();

  // Helper: Pobiera zdarzenia nakładające się na dany dzień
  const getEventsForDay = (day: number) => {
    return scheduledEvents
      .filter((se) => day >= se.startDay && day < se.startDay + se.duration)
      .map((se) => ({
        ...se,
        eventDetails: availableEvents.find((me) => me.id === se.eventId)!, // korzystamy z availableEvents
      }))
      .filter((se) => se.eventDetails !== undefined);
  };

  return (
    <div className="flex flex-col h-full bg-mars-itemBackground rounded-2xl p-8 border border-mars-orange/10 shadow-lg relative">
      <div className="flex justify-between items-center mb-6 shrink-0">
        <h2 className="text-xl font-bold tracking-widest text-white uppercase">
          Harmonogram Misji
        </h2>
      </div>

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
                  {dayEvents.map((se) => (
                    <span
                      key={se.id}
                      title={se.eventDetails.name}
                      className={`truncate text-[10px] px-2 py-0.5 rounded-full font-semibold border ${
                        se.eventDetails.type === "pogodowe"
                          ? "bg-red-600/20 text-red-500 border-red-600/50"
                          : "bg-green-600/20 text-green-500 border-green-600/50"
                      }`}
                    >
                      {se.eventDetails.name}
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
          availableEvents={availableEvents} // Używamy z propsów widoku
          dayEvents={getEventsForDay(selectedDay)}
          onClose={() => setSelectedDay(null)}
          onSave={saveEvent} // Z hooka
          onDelete={deleteEvent} // Z hooka
        />
      )}
    </div>
  );
};
