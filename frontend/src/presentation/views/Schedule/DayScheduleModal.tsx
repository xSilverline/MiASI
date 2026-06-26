import React, { useState } from "react";
import { Check, Trash2, X } from "lucide-react";
import type {
  EventData,
  ScheduledEvent,
} from "../../../core/domain/entities/event.ts";

interface DayScheduleModalProps {
  day: number;
  availableEvents: EventData[];
  dayEvents: (ScheduledEvent & { eventDetails: EventData })[];
  onClose: () => void;
  onSave: (event: Omit<ScheduledEvent, "id">, editId?: string) => void;
  onDelete: (scheduleId: string) => void;
}

export const DayScheduleModal: React.FC<DayScheduleModalProps> = ({
  day,
  availableEvents,
  dayEvents,
  onClose,
  onSave,
  onDelete,
}) => {
  const [selectedEventId, setSelectedEventId] = useState<string>("");
  const [duration, setDuration] = useState<number>(1);
  const [editingId, setEditingId] = useState<string | null>(null);

  const handleEditClick = (event: ScheduledEvent) => {
    setSelectedEventId(event.eventId);
    setDuration(event.duration);
    setEditingId(event.id);
  };

  const handleSave = () => {
    if (!selectedEventId || duration < 1) return;

    onSave(
      {
        eventId: selectedEventId,
        startDay: day,
        duration,
      },
      editingId || undefined,
    );

    setSelectedEventId("");
    setDuration(1);
    setEditingId(null);
  };

  return (
    <div className="fixed inset-0 bg-black/60 flex items-center justify-center z-50 backdrop-blur-sm p-4">
      <div className="bg-mars-background border border-mars-orange rounded-xl w-full max-w-3xl flex flex-col relative shadow-2xl">
        <div className="relative p-8 pb-4 flex items-center justify-center border-b border-white/5">
          <h2 className="text-xl font-bold tracking-widest text-mars-orange uppercase">
            Dzień {day}
          </h2>
          <button
            onClick={onClose}
            className="absolute right-8 text-mars-orange hover:text-white transition-colors"
          >
            <X size={32} strokeWidth={2} />
          </button>
        </div>

        <div className="p-8 flex flex-col gap-8">
          <div className="flex gap-4 items-end w-full">
            <div className="flex-1 flex flex-col gap-2">
              <label className="text-sm font-semibold tracking-wide text-white uppercase text-center">
                Zdarzenie
              </label>
              <select
                value={selectedEventId}
                onChange={(e) => setSelectedEventId(e.target.value)}
                className="w-full bg-mars-itemBackground border border-mars-line rounded-md p-3 text-white focus:outline-none focus:border-mars-orange appearance-none"
              >
                <option value="" disabled>
                  {availableEvents.length > 0
                    ? "Wybierz zdarzenie..."
                    : "Brak zdarzeń w katalogu"}
                </option>
                {availableEvents.map((event) => (
                  <option key={event.id} value={event.id}>
                    {event.name || event.description || event.type}
                  </option>
                ))}
              </select>
            </div>

            <div className="w-60 shrink-0 flex flex-col gap-2">
              <label className="text-sm font-semibold tracking-wide text-white uppercase text-center whitespace-nowrap">
                Czas Trwania [SOL]
              </label>
              <input
                type="number"
                min="1"
                value={duration}
                onChange={(e) => setDuration(parseInt(e.target.value) || 1)}
                className="w-full bg-mars-itemBackground border border-mars-line rounded-md p-3 text-white focus:outline-none focus:border-mars-orange text-center"
              />
            </div>

            <button
              onClick={handleSave}
              disabled={!selectedEventId || availableEvents.length === 0}
              className="p-3 shrink-0 flex items-center justify-center aspect-square bg-mars-itemBackground border border-mars-line rounded-md text-green-500 hover:border-green-500 hover:bg-green-500/10 transition-colors disabled:opacity-50 disabled:cursor-not-allowed mb-[1px]"
            >
              <Check size={24} />
            </button>
          </div>

          <div className="bg-mars-itemBackground rounded-lg p-6 min-h-50 max-h-50 overflow-y-auto custom-scrollbar flex flex-col gap-3 pr-4">
            {dayEvents.length === 0 ? (
              <p className="text-center text-slate-500 my-auto text-sm uppercase tracking-wider">
                Brak zdarzeń w tym dniu
              </p>
            ) : (
              dayEvents.map((item) => (
                <div
                  key={item.id}
                  onClick={() => handleEditClick(item)}
                  className={`flex items-center justify-between p-3 rounded-md cursor-pointer transition-colors shrink-0 ${
                    editingId === item.id ? "bg-white/10" : "hover:bg-white/5"
                  }`}
                >
                  <div className="flex items-center gap-4 min-w-0">
                    <span
                      className={`px-3 py-1 rounded-full text-xs font-bold uppercase tracking-wider shrink-0 ${
                        item.eventDetails.type === "THREAT"
                          ? "bg-red-600 text-white"
                          : "bg-green-500 text-white"
                      }`}
                    >
                      {item.eventDetails.type === "THREAT" ? "Zagrożenie" : "Sukces"}
                    </span>
                    <span className="text-sm text-slate-200 truncate">
                      {item.eventDetails.name}
                      {item.duration > 1 && (
                        <span className="text-slate-500 ml-2 whitespace-nowrap">
                          (Dzień {day - item.startDay + 1} z {item.duration})
                        </span>
                      )}
                    </span>
                  </div>

                  <button
                    onClick={(e) => {
                      e.stopPropagation();
                      onDelete(item.id);
                    }}
                    className="text-red-500 hover:text-red-400 p-2 shrink-0 ml-2"
                  >
                    <Trash2 size={20} />
                  </button>
                </div>
              ))
            )}
          </div>
        </div>
      </div>
    </div>
  );
};
