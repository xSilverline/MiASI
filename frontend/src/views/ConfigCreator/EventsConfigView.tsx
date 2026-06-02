import React, { useState } from "react";
import { ChevronLeft, AlertTriangle, Check, X } from "lucide-react";
import type { EventData } from "../../types/events";

interface EventsConfigStepProps {
  events: EventData[];
  onPrev: () => void;
  onFinish: () => void;
  onAddEvent: () => void;
  onEditEvent: (id: string) => void;
  onDeleteEvent: (id: string) => void;
  standaloneMode?: boolean;
  onCancel?: () => void;
}

export const EventsConfigStep: React.FC<EventsConfigStepProps> = ({
  events,
  onPrev,
  onFinish,
  onAddEvent,
  onEditEvent,
  onDeleteEvent,
  standaloneMode = false,
  onCancel,
}) => {
  const [activeEventId, setActiveEventId] = useState<string>(
    events.length > 0 ? events[0].id : "",
  );
  const [isDeleteModalOpen, setIsDeleteModalOpen] = useState<boolean>(false);

  const handleDeleteConfirm = () => {
    onDeleteEvent(activeEventId);
    setIsDeleteModalOpen(false);

    const remainingEvents = events.filter((e) => e.id !== activeEventId);
    setActiveEventId(remainingEvents.length > 0 ? remainingEvents[0].id : "");
  };

  return (
    <div className="bg-mars-itemBackground py-12 px-6 rounded-4xl shadow-xl w-full flex flex-col h-160 relative">
      <h2 className="text-center font-bold tracking-widest text-sm uppercase mb-12 text-mars-orange">
        Zdarzenia
      </h2>

      <div className="flex justify-center gap-14 md:gap-30 w-full mb-8">
        <button
          onClick={onAddEvent}
          className="text-[10px] md:text-xs text-slate-300 hover:text-white tracking-widest uppercase transition-colors cursor-pointer"
        >
          Dodaj
        </button>
        <button
          onClick={() => activeEventId && onEditEvent(activeEventId)}
          disabled={!activeEventId}
          className="text-[10px] md:text-xs text-slate-300 hover:text-white tracking-widest uppercase transition-colors cursor-pointer disabled:opacity-50 disabled:cursor-not-allowed"
        >
          Edytuj
        </button>
        <button
          onClick={() => setIsDeleteModalOpen(true)}
          disabled={!activeEventId}
          className="text-[10px] md:text-xs text-slate-300 hover:text-white tracking-widest uppercase transition-colors cursor-pointer disabled:opacity-50 disabled:cursor-not-allowed"
        >
          Usuń
        </button>
      </div>

      <div className="text-mars-orange text-xs tracking-widest uppercase text-center mt-auto mb-4 font-bold">
        Nazwa
      </div>

      <div className="bg-mars-line rounded-xl mb-4 p-2 h-80 overflow-y-auto shadow-inner border border-mars-line/30 w-full">
        <ul className="flex flex-col">
          {events.length === 0 ? (
            <div className="flex items-center justify-center h-full text-slate-500 text-xs tracking-widest uppercase">
              Brak zdarzeń
            </div>
          ) : (
            events.map((ev) => {
              const isActive = activeEventId === ev.id;
              return (
                <li
                  key={ev.id}
                  onClick={() => setActiveEventId(ev.id)}
                  className={`
                    cursor-pointer transition-all duration-200 relative py-3 px-6 flex items-center justify-center overflow-hidden rounded-lg mb-1
                    ${isActive ? "text-white font-medium" : "text-slate-300 hover:text-white hover:bg-white/5"}
                  `}
                >
                  {isActive && (
                    <>
                      <div className="absolute inset-0 bg-linear-to-r from-mars-orange/40 via-mars-orange/10 to-transparent pointer-events-none" />
                      <div
                        className="absolute left-0 top-0 bottom-0 w-4 bg-linear-to-r from-mars-orange to-[#B33C12]"
                        style={{ clipPath: "polygon(0 0, 100% 50%, 0 100%)" }}
                      />
                    </>
                  )}
                  <span className="relative z-10 text-xs md:text-sm tracking-widest uppercase">
                    {ev.name}
                  </span>
                </li>
              );
            })
          )}
        </ul>
      </div>

      <div className="mt-auto flex justify-between w-full px-2">
        {standaloneMode ? (
          <>
            <button
              onClick={onCancel}
              className="text-red-500 hover:text-red-400 p-2 transition-all active:scale-95 cursor-pointer"
            >
              <X size={32} strokeWidth={3} />
            </button>
            <button
              onClick={onFinish}
              className="text-green-500 hover:text-green-400 p-2 transition-all active:scale-95 cursor-pointer"
            >
              <Check size={32} strokeWidth={3} />
            </button>
          </>
        ) : (
          <>
            <button
              onClick={onPrev}
              className="bg-mars-orange hover:bg-mars-orange/90 text-mars-background p-2.5 rounded-xl transition-all shadow-md active:scale-95 cursor-pointer"
            >
              <ChevronLeft size={24} strokeWidth={3} />
            </button>
            <button
              onClick={onFinish}
              className="bg-green-500 hover:bg-green-400 text-mars-background p-2.5 rounded-xl transition-all shadow-md active:scale-95 cursor-pointer"
            >
              <Check size={24} strokeWidth={3} />
            </button>
          </>
        )}
      </div>

      {isDeleteModalOpen && (
        <div className="absolute inset-0 z-50 flex items-center justify-center bg-mars-background/80 backdrop-blur-sm p-6">
          <div className="bg-mars-itemBackground border border-mars-line p-8 rounded-2xl shadow-2xl flex flex-col items-center max-w-sm text-center">
            <AlertTriangle
              size={48}
              className="text-red-500 mb-4"
              strokeWidth={1.5}
            />
            <h3 className="text-sm md:text-base font-bold tracking-widest text-white uppercase mb-4">
              Potwierdź usunięcie
            </h3>
            <p className="text-xs md:text-sm text-slate-300 mb-8 leading-relaxed tracking-wider">
              Czy na pewno chcesz usunąć wybrane zdarzenie z harmonogramu misji?
            </p>
            <div className="flex w-full gap-4">
              <button
                onClick={() => setIsDeleteModalOpen(false)}
                className="flex-1 py-3.5 px-4 rounded-xl text-xs tracking-widest font-bold uppercase border border-mars-line text-slate-300 hover:bg-mars-line/50 hover:text-white transition-colors cursor-pointer"
              >
                Anuluj
              </button>
              <button
                onClick={handleDeleteConfirm}
                className="flex-1 py-3.5 px-4 rounded-xl text-xs tracking-widest font-bold uppercase bg-red-500/10 text-red-500 border border-red-500/30 hover:bg-red-500/20 hover:border-red-500 transition-colors cursor-pointer"
              >
                Usuń
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
