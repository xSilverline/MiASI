// ======= ZMIANA ========
import React, { useState } from "react";
import { DayScheduleModal } from "./DayScheduleModal";
import { MOCK_EVENTS } from "../../types/events"; // Tymczasowo pobieramy dostępne zdarzenia z mocka
import type { ScheduledEvent } from "../../types/events";

interface ScheduleViewProps {
  missionDuration: number;
}

export const ScheduleView: React.FC<ScheduleViewProps> = ({
  missionDuration,
}) => {
  const days = Array.from({ length: missionDuration }, (_, i) => i + 1);

  // Stan: Zaplanowane zdarzenia (Docelowo powinno to być w useMissionData)
  const [scheduledEvents, setScheduledEvents] = useState<ScheduledEvent[]>([]);

  // Stan modala
  const [selectedDay, setSelectedDay] = useState<number | null>(null);

  // Helper: Pobiera zdarzenia nakładające się na dany dzień
  const getEventsForDay = (day: number) => {
    return (
      scheduledEvents
        .filter((se) => day >= se.startDay && day < se.startDay + se.duration)
        .map((se) => ({
          ...se,
          eventDetails: MOCK_EVENTS.find((me) => me.id === se.eventId)!,
        }))
        // Filtrujemy ewentualne sieroty (jeśli eventId nie istnieje w MOCK_EVENTS)
        .filter((se) => se.eventDetails !== undefined)
    );
  };

  const handleSaveEvent = (
    eventData: Omit<ScheduledEvent, "id">,
    editId?: string,
  ) => {
    if (editId) {
      setScheduledEvents((prev) =>
        prev.map((ev) => (ev.id === editId ? { ...ev, ...eventData } : ev)),
      );
    } else {
      const newEvent: ScheduledEvent = {
        ...eventData,
        id: `sched-${Date.now()}-${Math.random().toString(36).substr(2, 9)}`,
      };
      setScheduledEvents((prev) => [...prev, newEvent]);
    }
  };

  const handleDeleteEvent = (scheduleId: string) => {
    setScheduledEvents((prev) => prev.filter((ev) => ev.id !== scheduleId));
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
          availableEvents={MOCK_EVENTS}
          dayEvents={getEventsForDay(selectedDay)}
          onClose={() => setSelectedDay(null)}
          onSave={handleSaveEvent}
          onDelete={handleDeleteEvent}
        />
      )}
    </div>
  );
};
// ========= KONIEC SEKCJI========
