import React, { useState } from "react";
import { Users, Clock, Check, X } from "lucide-react";
import { Sidebar } from "./components/Sidebar.tsx";
import { HeaderCard } from "./components/HeaderCard.tsx";
import { DetailCard, type DetailItem } from "./components/DetailCard.tsx";
import { UsageChart } from "./components/UsageChart.tsx";
import { formatNumber } from "./utils/formatters.ts";
import {
  MISSION_DURATION,
  CREW_MEMBERS_NUMBER,
  OXYGEN_AMOUNT,
  WATER_AMOUNT,
  FOOD_AMOUNT,
  ENERGY_PRODUCTION,
  ENERGY_USAGE,
  ENERGY_DIFFERENCE,
} from "./config/config.ts";

export type ViewState = "login" | "dashboard" | "configCreator" | string;
import { LoginView } from "./views/LoginView.tsx";
import { ConfigCreatorView } from "./views/ConfigCreator/ConfigCreatorView.tsx";

import { type ModuleData, MOCK_MODULES } from "./types/module.ts";
import { type EventData, MOCK_EVENTS } from "./types/events.ts";

const App: React.FC = () => {
  const [currentView, setCurrentView] = useState<ViewState>("login");
  const MOCK_HAS_CONFIG = false;
  const [isManualWizard, setIsManualWizard] = useState(false);

  const [crew, setCrew] = useState({
    men: Math.floor(CREW_MEMBERS_NUMBER / 2),
    women: Math.ceil(CREW_MEMBERS_NUMBER / 2),
  });

  const [resources, setResources] = useState({
    oxygen: OXYGEN_AMOUNT,
    water: WATER_AMOUNT,
    food: FOOD_AMOUNT,
  });

  const [modulesList, setModulesList] = useState(
    MOCK_MODULES.map((mod) => ({ ...mod, count: 1 })),
  );

  const [eventsList, setEventsList] = useState<EventData[]>(MOCK_EVENTS);

  const [isCrewModalOpen, setIsCrewModalOpen] = useState(false);
  const [isResourcesModalOpen, setIsResourcesModalOpen] = useState(false);
  const [isModulesModalOpen, setIsModulesModalOpen] = useState(false);

  const [editCrew, setEditCrew] = useState(crew);
  const [editResources, setEditResources] = useState(resources);
  const [editModulesList, setEditModulesList] = useState(modulesList);

  const handleLogin = () => {
    setIsManualWizard(false);
    if (MOCK_HAS_CONFIG) {
      setCurrentView("dashboard");
    } else {
      setCurrentView("configCreator");
    }
  };

  const handleConfigFinish = (data?: {
    modules?: ModuleData[];
    events?: EventData[];
  }) => {
    if (data) {
      if (data.modules) {
        setModulesList((prev) => {
          return data.modules!.map((newMod) => {
            const existing = prev.find((p) => p.id === newMod.id);
            return { ...newMod, count: existing ? existing.count : 0 };
          });
        });
      }
      if (data.events) {
        setEventsList(data.events);
      }
    }
    setCurrentView("dashboard");
  };

  const openCrewModal = () => {
    setEditCrew(crew);
    setIsCrewModalOpen(true);
  };
  const openResourcesModal = () => {
    setEditResources(resources);
    setIsResourcesModalOpen(true);
  };
  const openModulesModal = () => {
    setEditModulesList(modulesList);
    setIsModulesModalOpen(true);
  };

  const totalCrew = crew.men + crew.women;

  const getPersonsWord = (count: number) => {
    if (count === 1) return "OSOBA";
    const lastDigit = count % 10;
    const lastTwoDigits = count % 100;
    if (lastTwoDigits >= 12 && lastTwoDigits <= 14) return "OSÓB";
    if (lastDigit >= 2 && lastDigit <= 4) return "OSOBY";
    return "OSÓB";
  };

  const resourceData: DetailItem[] = [
    { label: "TLEN", value: formatNumber(resources.oxygen), valueSuffix: "L" },
    { label: "WODA", value: formatNumber(resources.water), valueSuffix: "L" },
    {
      label: "ŻYWNOŚĆ",
      value: formatNumber(resources.food),
      valueSuffix: "PORCJI",
    },
  ];

  const typeMapping: Record<string, string> = {
    mieszkalny: "MIESZKALNE",
    produkcyjny: "PRODUKCYJNE",
    energetyczny: "ENERGETYCZNE",
    energia: "ENERGETYCZNE",
    magazynowy: "MAGAZYNOWE",
    uzytkowy: "UŻYTKOWE",
  };

  const groupedModules = modulesList.reduce(
    (acc, mod) => {
      const typeLabel =
        typeMapping[mod.type?.toLowerCase()] ||
        mod.type?.toUpperCase() ||
        "INNE";
      acc[typeLabel] = (acc[typeLabel] || 0) + mod.count;
      return acc;
    },
    {} as Record<string, number>,
  );

  const moduleData: DetailItem[] = Object.entries(groupedModules).map(
    ([label, value]) => ({
      label,
      value: value.toString(),
    }),
  );

  const energyData: DetailItem[] = [
    {
      label: "PRODUKCJA",
      value: formatNumber(ENERGY_PRODUCTION),
      valueSuffix: "W",
      color: "text-emerald-400",
    },
    {
      label: "ZUŻYCIE",
      value: formatNumber(ENERGY_USAGE),
      valueSuffix: "W",
      color: "text-red-400",
    },
    {
      label: "RÓŻNICA",
      value: formatNumber(ENERGY_DIFFERENCE),
      valueSuffix: "W",
      color: "text-white",
    },
  ];

  if (currentView === "login") return <LoginView onLogin={handleLogin} />;

  if (currentView === "configCreator")
    return (
      <ConfigCreatorView
        showStartWarning={isManualWizard}
        onFinish={handleConfigFinish}
        initialModules={modulesList}
        initialEvents={eventsList}
      />
    );
  if (currentView === "resources")
    return (
      <ConfigCreatorView
        standaloneView="resources"
        onFinish={handleConfigFinish}
        initialModules={modulesList}
        initialEvents={eventsList}
      />
    );
  if (currentView === "modules")
    return (
      <ConfigCreatorView
        standaloneView="modules"
        onFinish={handleConfigFinish}
        initialModules={modulesList}
        initialEvents={eventsList}
      />
    );
  if (currentView === "events")
    return (
      <ConfigCreatorView
        standaloneView="events"
        onFinish={handleConfigFinish}
        initialModules={modulesList}
        initialEvents={eventsList}
      />
    );

  return (
    <div className="flex h-screen w-screen bg-mars-background text-slate-100 font-sans overflow-hidden relative">
      <Sidebar
        currentView={currentView}
        onNavigate={(viewId) => {
          if (viewId === "configCreator") {
            setIsManualWizard(true);
          }
          setCurrentView(viewId);
        }}
        onLogout={() => setCurrentView("login")}
      />

      <main className="grow p-10 flex flex-col h-full min-w-0 gap-8 box-border">
        {currentView === "dashboard" ? (
          <>
            <div className="grid grid-cols-2 gap-8 w-full shrink-0">
              <HeaderCard
                icon={Users}
                title="ZAŁOGA"
                value={`${totalCrew} ${getPersonsWord(totalCrew)}`}
                onEdit={openCrewModal}
              />
              <HeaderCard
                icon={Clock}
                title="CZAS TRWANIA MISJI"
                value={`${MISSION_DURATION} SOL`}
              />
            </div>

            <UsageChart />

            <div className="grid grid-cols-3 gap-8 w-full shrink-0">
              <DetailCard
                title="ZASOBY POCZĄTKOWE"
                items={resourceData}
                onEdit={openResourcesModal}
              />
              <DetailCard
                title="Zestawienie modułów"
                items={moduleData}
                onEdit={openModulesModal}
              />
              <DetailCard title="BILANS ENERGII" items={energyData} />
            </div>
          </>
        ) : (
          <div className="flex items-center justify-center h-full">
            <h2 className="text-2xl text-slate-400 uppercase tracking-widest">
              WIDOK "{currentView}" W BUDOWIE...
            </h2>
          </div>
        )}
      </main>

      {isCrewModalOpen && (
        <div className="absolute inset-0 z-50 flex items-center justify-center bg-mars-background/80 backdrop-blur-sm p-6">
          <div className="bg-mars-itemBackground border border-mars-line p-10 rounded-3xl shadow-2xl flex flex-col items-center max-w-sm w-full animate-in fade-in zoom-in duration-300">
            <h3 className="text-sm md:text-base font-bold tracking-widest text-mars-orange uppercase mb-8">
              Edycja Załogi
            </h3>
            <div className="flex flex-col w-full gap-5 mb-10">
              <div className="grid grid-cols-[100px_1fr] items-center gap-4">
                <label className="text-[10px] tracking-widest uppercase text-slate-300 text-right">
                  Mężczyźni
                </label>
                <input
                  type="number"
                  min="0"
                  value={editCrew.men}
                  onChange={(e) =>
                    setEditCrew((p) => ({
                      ...p,
                      men: Number(e.target.value) || 0,
                    }))
                  }
                  className="w-full bg-mars-line text-white px-4 py-2.5 rounded-xl text-center text-sm focus:outline-none focus:ring-1 focus:ring-mars-orange/40"
                />
              </div>
              <div className="grid grid-cols-[100px_1fr] items-center gap-4">
                <label className="text-[10px] tracking-widest uppercase text-slate-300 text-right">
                  Kobiety
                </label>
                <input
                  type="number"
                  min="0"
                  value={editCrew.women}
                  onChange={(e) =>
                    setEditCrew((p) => ({
                      ...p,
                      women: Number(e.target.value) || 0,
                    }))
                  }
                  className="w-full bg-mars-line text-white px-4 py-2.5 rounded-xl text-center text-sm focus:outline-none focus:ring-1 focus:ring-mars-orange/40"
                />
              </div>
            </div>
            <div className="flex w-full gap-4">
              <button
                onClick={() => setIsCrewModalOpen(false)}
                className="flex-1 py-3 flex justify-center rounded-xl border border-mars-line text-slate-300 hover:bg-mars-line/50 hover:text-red-400 transition-colors cursor-pointer"
              >
                <X size={24} />
              </button>
              <button
                onClick={() => {
                  setCrew(editCrew);
                  setIsCrewModalOpen(false);
                }}
                className="flex-1 py-3 flex justify-center rounded-xl bg-green-500/10 text-green-500 border border-green-500/30 hover:bg-green-500/20 transition-colors cursor-pointer"
              >
                <Check size={24} />
              </button>
            </div>
          </div>
        </div>
      )}

      {isResourcesModalOpen && (
        <div className="absolute inset-0 z-50 flex items-center justify-center bg-mars-background/80 backdrop-blur-sm p-6">
          <div className="bg-mars-itemBackground border border-mars-line p-10 rounded-3xl shadow-2xl flex flex-col items-center max-w-sm w-full animate-in fade-in zoom-in duration-300">
            <h3 className="text-sm md:text-base font-bold tracking-widest text-mars-orange uppercase mb-8">
              Zasoby Początkowe
            </h3>
            <div className="flex flex-col w-full gap-5 mb-10">
              {Object.entries({
                oxygen: "Tlen (L)",
                water: "Woda (L)",
                food: "Żywność (Porcje)",
              }).map(([key, label]) => (
                <div
                  key={key}
                  className="grid grid-cols-[100px_1fr] items-center gap-4"
                >
                  <label className="text-[10px] tracking-widest uppercase text-slate-300 text-right">
                    {label}
                  </label>
                  <input
                    type="number"
                    min="0"
                    value={editResources[key as keyof typeof editResources]}
                    onChange={(e) =>
                      setEditResources((p) => ({
                        ...p,
                        [key]: Number(e.target.value) || 0,
                      }))
                    }
                    className="w-full bg-mars-line text-white px-4 py-2.5 rounded-xl text-center text-sm focus:outline-none focus:ring-1 focus:ring-mars-orange/40"
                  />
                </div>
              ))}
            </div>
            <div className="flex w-full gap-4">
              <button
                onClick={() => setIsResourcesModalOpen(false)}
                className="flex-1 py-3 flex justify-center rounded-xl border border-mars-line text-slate-300 hover:bg-mars-line/50 hover:text-red-400 transition-colors cursor-pointer"
              >
                <X size={24} />
              </button>
              <button
                onClick={() => {
                  setResources(editResources);
                  setIsResourcesModalOpen(false);
                }}
                className="flex-1 py-3 flex justify-center rounded-xl bg-green-500/10 text-green-500 border border-green-500/30 hover:bg-green-500/20 transition-colors cursor-pointer"
              >
                <Check size={24} />
              </button>
            </div>
          </div>
        </div>
      )}

      {isModulesModalOpen && (
        <div className="absolute inset-0 z-50 flex items-center justify-center bg-mars-background/80 backdrop-blur-sm p-6">
          <div className="bg-mars-itemBackground border border-mars-line p-10 rounded-3xl shadow-2xl flex flex-col items-center max-w-md w-full animate-in fade-in zoom-in duration-300">
            <h3 className="text-sm md:text-base font-bold tracking-widest text-mars-orange uppercase mb-8">
              Zestawienie Modułów
            </h3>
            <div className="flex flex-col w-full gap-3 mb-10 max-h-[50vh] overflow-y-auto pr-2">
              {editModulesList.map((mod) => (
                <div
                  key={mod.id}
                  className="grid grid-cols-[1fr_90px] items-center gap-4 py-1"
                >
                  <label
                    className="text-[10px] md:text-xs tracking-widest uppercase text-slate-300 text-left truncate"
                    title={mod.name}
                  >
                    {mod.name}
                  </label>
                  <input
                    type="number"
                    min="0"
                    value={mod.count}
                    onChange={(e) => {
                      const val = Number(e.target.value) || 0;
                      setEditModulesList((prev) =>
                        prev.map((m) =>
                          m.id === mod.id ? { ...m, count: val } : m,
                        ),
                      );
                    }}
                    className="w-full bg-mars-line text-white px-2 py-2.5 rounded-xl text-center text-sm focus:outline-none focus:ring-1 focus:ring-mars-orange/40"
                  />
                </div>
              ))}
            </div>
            <div className="flex w-full gap-4 mt-auto">
              <button
                onClick={() => setIsModulesModalOpen(false)}
                className="flex-1 py-3 flex justify-center rounded-xl border border-mars-line text-slate-300 hover:bg-mars-line/50 hover:text-red-400 transition-colors cursor-pointer"
              >
                <X size={24} />
              </button>
              <button
                onClick={() => {
                  setModulesList(editModulesList);
                  setIsModulesModalOpen(false);
                }}
                className="flex-1 py-3 flex justify-center rounded-xl bg-green-500/10 text-green-500 border border-green-500/30 hover:bg-green-500/20 transition-colors cursor-pointer"
              >
                <Check size={24} />
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default App;
