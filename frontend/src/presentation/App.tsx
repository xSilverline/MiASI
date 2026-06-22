import React, { useState } from "react";
import { Users, Clock, AlertTriangle, RefreshCw, Loader2 } from "lucide-react";
import { Sidebar } from "./components/Sidebar.tsx";
import { HeaderCard } from "./components/HeaderCard.tsx";
import { DetailCard, type DetailItem } from "./components/DetailCard.tsx";
import { UsageChart } from "./components/UsageChart.tsx";
import { formatNumber } from "./utils/formatters.ts";
import {
  ENERGY_PRODUCTION,
  ENERGY_USAGE,
  ENERGY_DIFFERENCE,
} from "../infrastructure/mock-data/config.ts";
import { ScheduleView } from "./views/Schedule/ScheduleView.tsx";

export type ViewState = "login" | "dashboard" | "configCreator" | string;
import { LoginView } from "./views/LoginView.tsx";
import {
  ConfigCreatorView,
  type StandaloneViewType,
} from "./views/ConfigCreator/ConfigCreatorView.tsx";

import type { ModuleData } from "../core/domain/entities/module.ts";
import type { EventData } from "../core/domain/entities/event.ts";

import { useMissionData } from "./hooks/useMissionData.ts";

import {
  CrewModal,
  ResourcesModal,
  ModulesModal,
} from "./components/DashboardModals.tsx";
import type { ResourceConsumption } from "../core/domain/value-objects/ResourceConsuption.ts";
import type {
  MissionDashboardConfig,
  ModuleWithCount,
} from "../core/domain/entities/MissionConfig.ts";

const App: React.FC = () => {
  const [currentView, setCurrentView] = useState<ViewState>("login");
  const [isManualWizard, setIsManualWizard] = useState(false);

  const {
    config,
    isLoading,
    isDataModified,
    isRecalculating,
    updateConfig,
    recalculate,
  } = useMissionData();

  const [isCrewModalOpen, setIsCrewModalOpen] = useState(false);
  const [isResourcesModalOpen, setIsResourcesModalOpen] = useState(false);
  const [isModulesModalOpen, setIsModulesModalOpen] = useState(false);

  const handleLogin = () => {
    setIsManualWizard(false);
    setCurrentView("dashboard");
  };

  const handleConfigFinish = (data?: {
    modules?: ModuleData[];
    events?: EventData[];
    consumption?: ResourceConsumption;
  }) => {
    if (data && config) {
      let hasChanges = false;
      const updates: Partial<MissionDashboardConfig> = {};

      if (
        data.modules &&
        JSON.stringify(data.modules) !== JSON.stringify(config.modulesList)
      ) {
        updates.modulesList = data.modules.map((newMod) => {
          const existing = config.modulesList.find((p) => p.id === newMod.id);
          return {
            ...newMod,
            count: existing ? existing.count : 0,
          } as ModuleWithCount;
        });
        hasChanges = true;
      }
      if (
        data.events &&
        JSON.stringify(data.events) !== JSON.stringify(config.eventsList)
      ) {
        updates.eventsList = data.events;
        hasChanges = true;
      }
      if (
        data.consumption &&
        JSON.stringify(data.consumption) !==
          JSON.stringify(config.consumptionRates)
      ) {
        updates.consumptionRates = data.consumption;
        hasChanges = true;
      }

      if (hasChanges) {
        void updateConfig(updates);
      }
    }
    setCurrentView("dashboard");
  };

  React.useEffect(() => {
    // Przekierowanie do kreatora, jeśli zalogowaliśmy się, skończyło się ładowanie, a nie ma configu
    if (!isLoading && !config && currentView === "dashboard") {
      setCurrentView("configCreator");
    }
  }, [isLoading, config, currentView]);

  // 1. Loader wyświetlamy TYLKO podczas faktycznego ładowania danych
  if (isLoading) {
    return (
      <div className="h-screen w-screen bg-mars-background flex flex-col items-center justify-center text-mars-orange">
        <Loader2 size={48} className="animate-spin mb-4" />
        <p className="tracking-widest uppercase font-bold text-sm">
          Nawiązywanie połączenia z bazą...
        </p>
      </div>
    );
  }

  const getStandaloneView = (): StandaloneViewType => {
    if (currentView === "resources") return "resources";
    if (currentView === "modules") return "modules";
    if (currentView === "events") return "events";
    return null;
  };

  // 2. Widoki, które mogą działać BEZ załadowanego configu
  if (currentView === "login") return <LoginView onLogin={handleLogin} />;

  if (
    ["configCreator", "resources", "modules", "events"].includes(currentView)
  ) {
    return (
      <ConfigCreatorView
        standaloneView={getStandaloneView()}
        showStartWarning={isManualWizard}
        onFinish={handleConfigFinish}
        initialModules={config?.modulesList || []}
        initialEvents={config?.eventsList || []}
        initialConsumption={config?.consumptionRates}
      />
    );
  }

  // 3. Zabezpieczenie: Wszystko poniżej TEJ linii (Dashboard, Harmonogram) wymaga configu.
  if (!config) return null;

  // --- ODTWORZONA LOGIKA WYLICZEŃ DLA STAREGO TYPU (MissionDashboardConfig) ---
  const totalCrew = config.crew.men + config.crew.women;

  const getPersonsWord = (count: number) => {
    if (count === 1) return "OSOBA";
    if (count >= 2 && count <= 4) return "OSOBY";
    return "OSÓB";
  };

  const resourceData: DetailItem[] = [
    {
      label: "TLEN",
      value: formatNumber(config.resources.oxygen),
      valueSuffix: "L",
    },
    {
      label: "WODA",
      value: formatNumber(config.resources.water),
      valueSuffix: "L",
    },
    {
      label: "ŻYWNOŚĆ",
      value: formatNumber(config.resources.food),
      valueSuffix: "PORCJI",
    },
  ];

  const moduleData: DetailItem[] = config.modulesList.map((m) => ({
    label: m.name.toUpperCase(),
    value: m.count.toString(),
  }));

  const energyData: DetailItem[] = [
    {
      label: "PRODUKCJA",
      value: formatNumber(ENERGY_PRODUCTION),
      valueSuffix: "kW",
    },
    { label: "ZUŻYCIE", value: formatNumber(ENERGY_USAGE), valueSuffix: "kW" },
    {
      label: "BILANS",
      value: formatNumber(ENERGY_DIFFERENCE),
      valueSuffix: "kW",
    },
  ];

  return (
    <div className="flex h-screen w-screen bg-mars-background text-slate-100 font-sans overflow-hidden relative">
      <Sidebar
        currentView={currentView}
        onNavigate={(v) => {
          if (v === "configCreator") setIsManualWizard(true);
          setCurrentView(v);
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
                onEdit={() => setIsCrewModalOpen(true)}
              />
              <HeaderCard
                icon={Clock}
                title="CZAS TRWANIA MISJI"
                value={`${config.missionDuration} SOL`}
              />
            </div>

            <div className="flex-1 min-h-0 w-full flex flex-col relative">
              {isDataModified ? (
                <div className="flex-1 flex flex-col justify-center items-center bg-mars-itemBackground rounded-3xl p-10 shadow-md border border-mars-orange/20 min-h-[300px]">
                  <div className="flex flex-col items-center text-center animate-in fade-in zoom-in duration-300">
                    <AlertTriangle
                      size={48}
                      className="text-mars-orange mb-4"
                      strokeWidth={1.5}
                    />
                    <h3 className="text-sm md:text-base font-bold tracking-widest text-white uppercase mb-3">
                      Zmieniono Parametry Systemu
                    </h3>
                    <p className="text-xs md:text-sm text-slate-400 mb-8 max-w-lg leading-relaxed">
                      Zaktualizowano dane. Przeprowadź rekalkulację, aby pobrać
                      najnowsze dane telemetryczne.
                    </p>
                    <button
                      onClick={() => {
                        void recalculate();
                      }}
                      disabled={isRecalculating}
                      className="flex items-center gap-3 py-3.5 px-8 rounded-xl text-xs tracking-widest font-bold uppercase bg-mars-orange/10 text-mars-orange border border-mars-orange/30 hover:bg-mars-orange/20 hover:border-mars-orange transition-all disabled:opacity-50 disabled:cursor-wait"
                    >
                      {isRecalculating ? (
                        <>
                          <RefreshCw size={18} className="animate-spin" />
                          Trwa Obliczanie...
                        </>
                      ) : (
                        <>Rekalkuluj Wykresy</>
                      )}
                    </button>
                  </div>
                </div>
              ) : (
                <UsageChart />
              )}
            </div>

            <div className="grid grid-cols-3 gap-8 w-full shrink-0">
              <DetailCard
                title="ZASOBY POCZĄTKOWE"
                items={resourceData}
                onEdit={() => setIsResourcesModalOpen(true)}
              />
              <DetailCard
                title="Zestawienie modułów"
                items={moduleData}
                onEdit={() => setIsModulesModalOpen(true)}
              />
              <DetailCard title="BILANS ENERGII" items={energyData} />
            </div>
          </>
        ) : currentView === "schedule" ? (
          <ScheduleView
            missionDuration={config.missionDuration}
            availableEvents={config.eventsList}
          />
        ) : (
          <div className="flex items-center justify-center h-full">
            <h2 className="text-2xl text-slate-400 uppercase tracking-widest">
              WIDOK "{currentView}" W BUDOWIE...
            </h2>
          </div>
        )}
      </main>

      {isCrewModalOpen && (
        <CrewModal
          data={config.crew}
          onClose={() => setIsCrewModalOpen(false)}
          onSave={(d) => {
            void updateConfig({ crew: d });
          }}
        />
      )}

      {isResourcesModalOpen && (
        <ResourcesModal
          data={config.resources}
          onClose={() => setIsResourcesModalOpen(false)}
          onSave={(d) => {
            void updateConfig({ resources: d });
          }}
        />
      )}

      {isModulesModalOpen && (
        <ModulesModal
          data={config.modulesList}
          onClose={() => setIsModulesModalOpen(false)}
          onSave={(d) => {
            void updateConfig({ modulesList: d });
          }}
        />
      )}
    </div>
  );
};

export default App;
