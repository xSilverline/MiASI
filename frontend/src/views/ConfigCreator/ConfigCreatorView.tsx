import React, { useState } from "react";
import { AlertTriangle, Save } from "lucide-react";
import earthBg from "../../assets/earth.png";
import marsBg from "../../assets/mars.png";

import { ResourceConfigStep } from "./ResourceConfigView";
import { ModulesConfigStep } from "./ModulesConfigView";
import { AddModuleStep } from "./AddEditModuleView";
import { EventsConfigStep } from "./EventsConfigView";
import { AddEventStep } from "./AddEditEventView";

import type { ModuleData } from "../../types/module";
import { MOCK_MODULES } from "../../types/module";
import type { EventData } from "../../types/events";
import { MOCK_EVENTS } from "../../types/events";

export type StandaloneViewType = "resources" | "modules" | "events" | null;

interface ConfigCreatorViewProps {
  standaloneView?: StandaloneViewType;
  initialModules?: ModuleData[];
  initialEvents?: EventData[];
  showStartWarning?: boolean;
  onFinish: (data?: { modules?: ModuleData[]; events?: EventData[] }) => void;
}

export const ConfigCreatorView: React.FC<ConfigCreatorViewProps> = ({
  standaloneView = null,
  initialModules,
  initialEvents,
  showStartWarning = false,
  onFinish,
}) => {
  const getInitialStep = () => {
    if (standaloneView === "resources") return 1;
    if (standaloneView === "modules") return 2;
    if (standaloneView === "events") return 5;
    return 1;
  };

  const [step, setStep] = useState<number>(getInitialStep());

  const isStandalone = !!standaloneView;
  const [isStartModalOpen, setIsStartModalOpen] =
    useState<boolean>(showStartWarning);
  const [isFinishModalOpen, setIsFinishModalOpen] = useState<boolean>(false);

  const [modules, setModules] = useState<ModuleData[]>(
    initialModules || MOCK_MODULES,
  );
  const [editingModuleId, setEditingModuleId] = useState<string | null>(null);

  const [events, setEvents] = useState<EventData[]>(
    initialEvents || MOCK_EVENTS,
  );
  const [editingEventId, setEditingEventId] = useState<string | null>(null);

  const handleFinalSave = () => {
    onFinish({ modules, events });
  };

  const handleCancel = () => {
    onFinish();
  };

  const getStepTitle = () => {
    switch (step) {
      case 1:
        return "Konfiguracja Zużycia Zasobów";
      case 2:
      case 3:
      case 4:
        return "Konfiguracja Modułów";
      case 5:
      case 6:
      case 7:
        return "Konfiguracja Zdarzeń";
      default:
        return "";
    }
  };

  const handleAddModule = (newModule: Omit<ModuleData, "id">) => {
    setModules([...modules, { ...newModule, id: crypto.randomUUID() }]);
    setStep(2);
  };
  const handleEditModule = (updatedModule: ModuleData) => {
    setModules(
      modules.map((m) => (m.id === updatedModule.id ? updatedModule : m)),
    );
    setStep(2);
  };

  const handleAddEvent = (newEvent: Omit<EventData, "id">) => {
    setEvents([
      ...events,
      { ...newEvent, id: crypto.randomUUID() } as EventData,
    ]);
    setStep(5);
  };
  const handleEditEvent = (updatedEvent: EventData) => {
    setEvents(events.map((e) => (e.id === updatedEvent.id ? updatedEvent : e)));
    setStep(5);
  };

  const moduleToEdit = modules.find((m) => m.id === editingModuleId);
  const eventToEdit = events.find((e) => e.id === editingEventId);

  const isWideStep = step === 6 || step === 7;

  return (
    <div className="h-screen w-screen bg-mars-background font-sans flex flex-col items-center justify-center relative overflow-hidden text-slate-100 select-none">
      <div className="absolute left-0 top-1/2 -translate-x-1/2 -translate-y-1/2 w-[45vw] h-[90vh] opacity-40 pointer-events-none bg-radial from-blue-900/20 to-transparent rounded-full blur-2xl lg:opacity-100" />
      <img
        src={earthBg}
        className="absolute left-0 top-1/2 -translate-x-1/2 -translate-y-1/2 h-[90vh] w-auto object-contain pointer-events-none select-none hidden lg:block opacity-25"
        alt="Earth"
      />

      <div className="absolute right-0 top-1/2 translate-x-1/2 -translate-y-1/2 w-[45vw] h-[90vh] opacity-40 pointer-events-none bg-radial from-mars-orange/10 to-transparent rounded-full blur-2xl lg:opacity-100" />
      <img
        src={marsBg}
        className="absolute right-0 top-1/2 translate-x-1/2 -translate-y-1/2 h-[90vh] w-auto object-contain pointer-events-none select-none hidden lg:block opacity-25"
        alt="Mars"
      />

      <div
        className={`flex flex-col items-center z-10 w-full px-6 transition-all duration-500 ${isWideStep ? "max-w-7xl" : "max-w-2xl"}`}
      >
        <h1 className="text-xl md:text-2xl font-bold tracking-widest uppercase mb-8 flex gap-3">
          <span className="text-gradient-mars">
            {isStandalone ? "Edycja" : "Kreator"} Konfiguracji
          </span>
        </h1>

        <div className="bg-mars-itemBackground px-8 py-4 rounded-2xl mb-10 shadow-md">
          <span className="tracking-widest text-xs md:text-sm font-medium text-slate-200 uppercase transition-all">
            {getStepTitle()}
          </span>
        </div>

        {step === 1 && (
          <ResourceConfigStep
            onNext={() =>
              isStandalone ? setIsFinishModalOpen(true) : setStep(2)
            }
            standaloneMode={isStandalone}
            onCancel={onFinish}
          />
        )}

        {step === 2 && (
          <ModulesConfigStep
            modules={modules}
            onPrev={() => setStep(1)}
            onFinish={() =>
              isStandalone ? setIsFinishModalOpen(true) : setStep(5)
            }
            onAddModule={() => setStep(3)}
            onEditModule={(id) => {
              setEditingModuleId(id);
              setStep(4);
            }}
            onDeleteModule={(id) =>
              setModules(modules.filter((m) => m.id !== id))
            }
            standaloneMode={isStandalone}
            onCancel={onFinish}
          />
        )}

        {step === 3 && (
          <AddModuleStep
            mode="add"
            onCancel={() => setStep(2)}
            onSave={(data) => handleAddModule(data as Omit<ModuleData, "id">)}
          />
        )}
        {step === 4 && moduleToEdit && (
          <AddModuleStep
            mode="edit"
            initialData={moduleToEdit}
            onCancel={() => setStep(2)}
            onSave={(data) => handleEditModule(data as ModuleData)}
          />
        )}

        {step === 5 && (
          <EventsConfigStep
            events={events}
            onPrev={() => setStep(2)}
            onFinish={() => setIsFinishModalOpen(true)}
            onAddEvent={() => setStep(6)}
            onEditEvent={(id) => {
              setEditingEventId(id);
              setStep(7);
            }}
            onDeleteEvent={(id) => setEvents(events.filter((e) => e.id !== id))}
            standaloneMode={isStandalone}
            onCancel={onFinish}
          />
        )}

        {step === 6 && (
          <AddEventStep
            mode="add"
            modules={modules}
            onCancel={() => setStep(5)}
            onSave={(data) => handleAddEvent(data as Omit<EventData, "id">)}
          />
        )}
        {step === 7 && eventToEdit && (
          <AddEventStep
            mode="edit"
            modules={modules}
            initialData={eventToEdit}
            onCancel={() => setStep(5)}
            onSave={(data) => handleEditEvent(data as EventData)}
          />
        )}
      </div>

      {isStartModalOpen && (
        <div className="absolute inset-0 z-50 flex items-center justify-center bg-mars-background/80 backdrop-blur-sm p-6">
          <div className="bg-mars-itemBackground border border-mars-line p-8 rounded-2xl shadow-2xl flex flex-col items-center max-w-sm text-center animate-in fade-in zoom-in duration-300">
            <AlertTriangle
              size={48}
              className="text-red-500 mb-4"
              strokeWidth={1.5}
            />
            <h3 className="text-sm md:text-base font-bold tracking-widest text-white uppercase mb-4">
              Nowa Konfiguracja
            </h3>
            <p className="text-xs md:text-sm text-slate-300 mb-8 leading-relaxed tracking-wider">
              Rozpoczęcie kreatora spowoduje usunięcie obecnej, zapisanej
              konfiguracji bazy. Czy na pewno chcesz kontynuować?
            </p>
            <div className="flex w-full gap-4">
              <button
                onClick={handleCancel}
                className="flex-1 py-3.5 px-4 rounded-xl text-xs tracking-widest font-bold uppercase border border-mars-line text-slate-300 hover:bg-mars-line/50 hover:text-white transition-colors cursor-pointer"
              >
                Anuluj
              </button>
              <button
                onClick={() => setIsStartModalOpen(false)}
                className="flex-1 py-3.5 px-4 rounded-xl text-xs tracking-widest font-bold uppercase bg-red-500/10 text-red-500 border border-red-500/30 hover:bg-red-500/20 hover:border-red-500 transition-colors cursor-pointer"
              >
                Rozpocznij
              </button>
            </div>
          </div>
        </div>
      )}

      {isFinishModalOpen && (
        <div className="absolute inset-0 z-50 flex items-center justify-center bg-mars-background/80 backdrop-blur-sm p-6">
          <div className="bg-mars-itemBackground border border-mars-line p-8 rounded-2xl shadow-2xl flex flex-col items-center max-w-sm text-center animate-in fade-in zoom-in duration-300">
            <Save size={48} className="text-green-500 mb-4" strokeWidth={1.5} />
            <h3 className="text-sm md:text-base font-bold tracking-widest text-white uppercase mb-4">
              Zapisz Konfigurację
            </h3>
            <p className="text-xs md:text-sm text-slate-300 mb-8 leading-relaxed tracking-wider">
              Czy na pewno chcesz zapisać wprowadzone dane i zakończyć
              konfigurację?
            </p>
            <div className="flex w-full gap-4">
              <button
                onClick={() => setIsFinishModalOpen(false)}
                className="flex-1 py-3.5 px-4 rounded-xl text-xs tracking-widest font-bold uppercase border border-mars-line text-slate-300 hover:bg-mars-line/50 hover:text-white transition-colors cursor-pointer"
              >
                Wróć
              </button>
              <button
                onClick={handleFinalSave}
                className="flex-1 py-3.5 px-4 rounded-xl text-xs tracking-widest font-bold uppercase bg-green-500/10 text-green-500 border border-green-500/30 hover:bg-green-500/20 hover:border-green-500 transition-colors cursor-pointer"
              >
                Zapisz
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
