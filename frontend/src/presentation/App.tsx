import React, { useState } from "react";
import {
  Users,
  Clock,
  AlertTriangle,
  RefreshCw,
  Loader2,
  Settings,
} from "lucide-react";
import { Sidebar } from "./components/Sidebar.tsx";
import { HeaderCard } from "./components/HeaderCard.tsx";
import { DetailCard, type DetailItem } from "./components/DetailCard.tsx";
import { UsageChart } from "./components/UsageChart.tsx";
import { formatNumber } from "./utils/formatters.ts";
import { ScheduleView } from "./views/Schedule/ScheduleView.tsx";
import { SimulationView } from "./views/Simulation/SimulationView.tsx";
import { LoginView } from "./views/LoginView.tsx";
import {
  ConfigCreatorView,
  type StandaloneViewType,
} from "./views/ConfigCreator/ConfigCreatorView.tsx";
import type { ModuleData } from "../core/domain/entities/module.ts";
import type { EventData } from "../core/domain/entities/event.ts";
import type { GeneralConfigData } from "./views/ConfigCreator/ResourceConfigView.tsx";
import { useMissionData } from "./hooks/useMissionData.ts";
import {
  CrewModal,
  ResourcesModal,
  ModulesModal,
} from "./components/DashboardModals.tsx";
import type {
  MissionDashboardConfig,
  ModuleWithCount,
} from "../core/domain/entities/MissionConfig.ts";

export type ViewState = "login" | "dashboard" | "configCreator" | string;

const App: React.FC = () => {
  const [currentView, setCurrentView] = useState<ViewState>("login");
  const [isManualWizard, setIsManualWizard] = useState(false);
  const [isInitializing, setIsInitializing] = useState(true);

  const {
    config,
    optimizedConfig,
    isLoading,
    isDataModified,
    isRecalculating,
    isOptimizing,
    analysisError,
    payloadSessionId,
    nominalSessionId,
    chartData,
    updateConfig,
    recalculate,
    optimize,
    loadConfig,
    resetMissionData,
  } = useMissionData();

  const [isCrewModalOpen, setIsCrewModalOpen] = useState(false);
  const [isResourcesModalOpen, setIsResourcesModalOpen] = useState(false);
  const [isModulesModalOpen, setIsModulesModalOpen] = useState(false);

  React.useEffect(() => {
    const initApp = async () => {
      const token = localStorage.getItem("sessionToken");
      if (token) {
        const data = await loadConfig();
        setCurrentView(data ? "dashboard" : "configCreator");
      } else {
        setCurrentView("login");
      }
      setIsInitializing(false);
    };
    void initApp();
  }, [loadConfig]);

  const handleLogin = async () => {
    setIsManualWizard(false);
    const data = await loadConfig();
    setCurrentView(data ? "dashboard" : "configCreator");
  };

  const handleConfigFinish = (data?: {
    general?: GeneralConfigData;
    modules?: ModuleData[];
    events?: EventData[];
  }) => {
    if (data) {
      let hasChanges = false;
      const updates: Partial<MissionDashboardConfig> = {};

      if (data.general) {
        updates.missionDuration = data.general.missionDuration;
        updates.maxStartingWeight = data.general.maxStartingWeight;
        updates.crew = data.general.crew;
        updates.startingResources = data.general.startingResources;
        hasChanges = true;
      }

      if (data.modules) {
        updates.modulesList = data.modules.map((newMod) => {
          const existing = config?.modulesList?.find(
            (p) => p.id === newMod.id || p.name === newMod.name,
          );

          return {
            ...newMod,
            count: existing ? existing.count : 1,
          } as ModuleWithCount;
        });
        hasChanges = true;
      }

      if (data.events) {
        updates.eventsList = data.events;
        hasChanges = true;
      }

      if (hasChanges) {
        void updateConfig(updates);
      }
    }
    setCurrentView("dashboard");
  };

  if (isInitializing || (currentView !== "login" && isLoading)) {
    return (
      <div className="h-screen w-screen bg-mars-background flex flex-col items-center justify-center text-mars-orange">
        <Loader2 size={48} className="animate-spin mb-4" />
        <p className="tracking-widest uppercase font-bold text-sm">
          {isInitializing
            ? "Inicjalizacja Systemów..."
            : "Synchronizacja z Bazą..."}
        </p>
      </div>
    );
  }

  if (currentView === "login") return <LoginView onLogin={handleLogin} />;

  const getStandaloneView = (): StandaloneViewType => {
    if (currentView === "resources") return "resources";
    if (currentView === "modules") return "modules";
    if (currentView === "events") return "events";
    return null;
  };

  if (
    ["configCreator", "resources", "modules", "events"].includes(currentView)
  ) {
    return (
      <ConfigCreatorView
        standaloneView={getStandaloneView()}
        showStartWarning={isManualWizard}
        onFinish={handleConfigFinish}
        initialConfig={config}
      />
    );
  }

  if (!config) return null;

  const dashboardConfig: MissionDashboardConfig = optimizedConfig
    ? {
        ...config,
        ...optimizedConfig,
        startingResources:
          optimizedConfig.startingResources || config.startingResources,
        modulesList: optimizedConfig.modulesList || config.modulesList,
        crew: optimizedConfig.crew || config.crew,
        eventsList: optimizedConfig.eventsList || config.eventsList,
        missionDuration:
          optimizedConfig.missionDuration || config.missionDuration,
        maxStartingWeight:
          optimizedConfig.maxStartingWeight || config.maxStartingWeight,
      }
    : config;

  const totalCrew = dashboardConfig.crew.reduce(
    (acc, profile) => acc + (profile.population || 0),
    0,
  );
  const getPersonsWord = (count: number) => {
    if (count === 1) return "OSOBA";
    if (count >= 2 && count <= 4) return "OSOBY";
    return "OSÓB";
  };

  const getResQty = (type: string) =>
    dashboardConfig.startingResources.find((r) => r.resourceType === type)
      ?.quantity || 0;

  const resourceData: DetailItem[] = [
    {
      label: "TLEN",
      value: formatNumber(getResQty("OXYGEN")),
      valueSuffix: "L",
    },
    {
      label: "WODA",
      value: formatNumber(getResQty("WATER")),
      valueSuffix: "L",
    },
    {
      label: "ŻYWNOŚĆ",
      value: formatNumber(getResQty("FOOD")),
      valueSuffix: "PORCJI",
    },
  ];

  const moduleData: DetailItem[] = dashboardConfig.modulesList.map((m) => ({
    label: m.name.toUpperCase(),
    value: m.count.toString(),
  }));

  const getEnergyQuantity = (
    collection: { resourceType: string; quantity: number }[] | undefined,
  ) =>
    collection?.find((resource) => resource.resourceType === "ENERGY")
      ?.quantity || 0;

  const energyProduction = dashboardConfig.modulesList.reduce(
    (sum, module) =>
      sum + getEnergyQuantity(module.resourceProduction) * (module.count || 0),
    0,
  );

  const energyConsumption = dashboardConfig.modulesList.reduce(
    (sum, module) =>
      sum + getEnergyQuantity(module.resourceConsumption) * (module.count || 0),
    0,
  );

  const energyBalance = energyProduction - energyConsumption;

  const energyData: DetailItem[] = [
    {
      label: "PRODUKCJA",
      value: formatNumber(energyProduction),
      valueSuffix: "kW",
    },
    {
      label: "ZUŻYCIE",
      value: formatNumber(energyConsumption),
      valueSuffix: "kW",
    },
    {
      label: "BILANS",
      value: formatNumber(energyBalance),
      valueSuffix: "kW",
      color: energyBalance >= 0 ? "text-green-500" : "text-red-500",
    },
  ];

  return (
    <div className="flex h-screen w-screen bg-mars-background text-slate-100 font-sans overflow-hidden relative">
      <Sidebar
        currentView={currentView}
        onNavigate={(v) => {
          if (v === "configCreator") setIsManualWizard(true);
          else setIsManualWizard(false);
          setCurrentView(v);
        }}
        onLogout={() => {
          localStorage.removeItem("sessionToken");
          resetMissionData();
          setIsManualWizard(false);
          setIsCrewModalOpen(false);
          setIsResourcesModalOpen(false);
          setIsModulesModalOpen(false);
          setCurrentView("login");
        }}
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
                value={`${dashboardConfig.missionDuration} SOL`}
              />
            </div>

            <div className="flex-1 min-h-0 w-full flex flex-col relative">
              {isDataModified || !payloadSessionId ? (
                <div className="flex-1 flex flex-col justify-center items-center bg-mars-itemBackground rounded-3xl p-10 shadow-md border border-mars-orange/20 min-h-[300px]">
                  <div className="flex flex-col items-center text-center animate-in fade-in zoom-in duration-300">
                    <AlertTriangle
                      size={48}
                      className="text-mars-orange mb-4"
                      strokeWidth={1.5}
                    />
                    <h3 className="text-sm md:text-base font-bold tracking-widest text-white uppercase mb-3">
                      Wymagana Analiza i Optymalizacja
                    </h3>
                    <p className="text-xs md:text-sm text-slate-400 mb-8 max-w-lg leading-relaxed">
                      Wprowadzono zmiany w konfiguracji. System musi najpierw
                      przeprowadzić Auto-Optymalizację pakietu startowego, zanim
                      możliwa będzie rekalkulacja przebiegu misji.
                    </p>

                    <div className="flex flex-wrap items-center justify-center gap-4">
                      <button
                        onClick={() => void optimize()}
                        disabled={isOptimizing || isRecalculating}
                        className="flex items-center gap-3 py-3.5 px-8 rounded-xl text-xs tracking-widest font-bold uppercase bg-blue-500/10 text-blue-500 border border-blue-500/30 hover:bg-blue-500/20 hover:border-blue-500 transition-all disabled:opacity-50 disabled:cursor-wait"
                      >
                        {isOptimizing ? (
                          <Loader2 size={18} className="animate-spin" />
                        ) : (
                          <Settings size={18} />
                        )}
                        Auto Optymalizacja
                      </button>

                      <button
                        onClick={() => void recalculate()}
                        disabled={
                          !payloadSessionId || isRecalculating || isOptimizing
                        }
                        className="flex items-center gap-3 py-3.5 px-8 rounded-xl text-xs tracking-widest font-bold uppercase bg-mars-orange/10 text-mars-orange border border-mars-orange/30 hover:bg-mars-orange/20 hover:border-mars-orange transition-all disabled:opacity-50 disabled:cursor-not-allowed"
                      >
                        {isRecalculating ? (
                          <>
                            <RefreshCw size={18} className="animate-spin" />{" "}
                            Trwa Obliczanie...
                          </>
                        ) : (
                          <>Rekalkuluj Wykresy</>
                        )}
                      </button>
                    </div>
                  </div>
                </div>
              ) : isRecalculating ? (
                <div className="flex-1 flex flex-col justify-center items-center bg-mars-itemBackground rounded-3xl p-10 shadow-md border border-mars-orange/20 min-h-[300px]">
                  <Loader2
                    size={44}
                    className="animate-spin text-mars-orange mb-4"
                  />
                  <h3 className="text-sm md:text-base font-bold tracking-widest text-white uppercase mb-3">
                    Trwa symulacja nominalna
                  </h3>
                  <p className="text-xs md:text-sm text-slate-400 max-w-lg text-center leading-relaxed">
                    Auto-optymalizacja zakończona. System przelicza przebieg
                    misji i przygotowuje dane wykresu.
                  </p>
                </div>
              ) : analysisError ? (
                <div className="flex-1 flex flex-col justify-center items-center bg-mars-itemBackground rounded-3xl p-10 shadow-md border border-red-500/20 min-h-[300px]">
                  <AlertTriangle
                    size={48}
                    className="text-red-500 mb-4"
                    strokeWidth={1.5}
                  />
                  <h3 className="text-sm md:text-base font-bold tracking-widest text-white uppercase mb-3">
                    Nie udało się przeliczyć wykresu
                  </h3>
                  <p className="text-xs md:text-sm text-slate-400 mb-8 max-w-lg text-center leading-relaxed">
                    {analysisError}
                  </p>
                  <button
                    onClick={() => void recalculate()}
                    disabled={
                      !payloadSessionId || isRecalculating || isOptimizing
                    }
                    className="flex items-center gap-3 py-3.5 px-8 rounded-xl text-xs tracking-widest font-bold uppercase bg-mars-orange/10 text-mars-orange border border-mars-orange/30 hover:bg-mars-orange/20 hover:border-mars-orange transition-all disabled:opacity-50 disabled:cursor-not-allowed"
                  >
                    Ponów rekalkulację
                  </button>
                </div>
              ) : chartData.length === 0 ? (
                <div className="flex-1 flex flex-col justify-center items-center bg-mars-itemBackground rounded-3xl p-10 shadow-md border border-mars-orange/20 min-h-[300px]">
                  <AlertTriangle
                    size={48}
                    className="text-mars-orange mb-4"
                    strokeWidth={1.5}
                  />
                  <h3 className="text-sm md:text-base font-bold tracking-widest text-white uppercase mb-3">
                    Brak danych wykresu
                  </h3>
                  <p className="text-xs md:text-sm text-slate-400 mb-8 max-w-lg text-center leading-relaxed">
                    Payload został utworzony, ale symulacja nominalna nie
                    zwróciła danych timeline.
                  </p>
                  <button
                    onClick={() => void recalculate()}
                    disabled={
                      !payloadSessionId || isRecalculating || isOptimizing
                    }
                    className="flex items-center gap-3 py-3.5 px-8 rounded-xl text-xs tracking-widest font-bold uppercase bg-mars-orange/10 text-mars-orange border border-mars-orange/30 hover:bg-mars-orange/20 hover:border-mars-orange transition-all disabled:opacity-50 disabled:cursor-not-allowed"
                  >
                    Rekalkuluj wykres
                  </button>
                </div>
              ) : (
                <UsageChart
                  data={chartData}
                  missionDuration={dashboardConfig.missionDuration}
                />
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
            missionDuration={dashboardConfig.missionDuration}
            availableEvents={config.eventsList}
          />
        ) : currentView === "simAuto" ? (
          <SimulationView
            mode="automatic"
            missionDuration={dashboardConfig.missionDuration}
            nominalSessionId={nominalSessionId}
            availableEvents={config.eventsList}
          />
        ) : currentView === "simManual" ? (
          <SimulationView
            mode="manual"
            missionDuration={dashboardConfig.missionDuration}
            nominalSessionId={nominalSessionId}
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

      {/* UWAGA: Komponenty Modali będą teraz wymagały przepisania, bo dostają czyste arraye prosto z API! */}
      {isCrewModalOpen && (
        <CrewModal
          data={config.crew}
          onClose={() => setIsCrewModalOpen(false)}
          onSave={(d) => void updateConfig({ crew: d })}
        />
      )}

      {isResourcesModalOpen && (
        <ResourcesModal
          data={config.startingResources}
          onClose={() => setIsResourcesModalOpen(false)}
          onSave={(d) => void updateConfig({ startingResources: d })}
        />
      )}

      {isModulesModalOpen && (
        <ModulesModal
          data={config.modulesList}
          onClose={() => setIsModulesModalOpen(false)}
          onSave={(d) => void updateConfig({ modulesList: d })}
        />
      )}
    </div>
  );
};

export default App;
