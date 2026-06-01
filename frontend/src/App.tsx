import React, { useState } from "react";
import { Users, Clock } from "lucide-react";
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
  HOUSING_MODULE_COUNT,
  ENERGY_MODULE_COUNT,
  PRODUCTION_MODULE_COUNT,
  ENERGY_PRODUCTION,
  ENERGY_USAGE,
  ENERGY_DIFFERENCE,
} from "./config/config.ts";
export type ViewState = "login" | "dashboard" | "configCreator" | string;
import { LoginView } from "./views/LoginView.tsx";
import { ConfigCreatorView } from "./views/ConfigCreator/ConfigCreatorView.tsx";

const App: React.FC = () => {
  const [currentView, setCurrentView] = useState<ViewState>("login");
  const MOCK_HAS_CONFIG = false;
  const handleLogin = () => {
    if (MOCK_HAS_CONFIG) {
      setCurrentView("dashboard");
    } else {
      setCurrentView("configCreator");
    }
  };

  const resourceData: DetailItem[] = [
    { label: "TLEN", value: formatNumber(OXYGEN_AMOUNT), valueSuffix: "L" },
    { label: "WODA", value: formatNumber(WATER_AMOUNT), valueSuffix: "L" },
    {
      label: "ŻYWNOŚĆ",
      value: formatNumber(FOOD_AMOUNT),
      valueSuffix: "PORCJI",
    },
  ];

  const moduleData: DetailItem[] = [
    { label: "MIESZKALNY", value: HOUSING_MODULE_COUNT.toString() },
    { label: "ENERGETYCZNY", value: ENERGY_MODULE_COUNT.toString() },
    { label: "PRODUKCYJNY", value: PRODUCTION_MODULE_COUNT.toString() },
  ];

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

  if (currentView === "login") {
    return <LoginView onLogin={handleLogin} />;
  }

  if (currentView === "configCreator") {
    return <ConfigCreatorView onFinish={() => setCurrentView("dashboard")} />;
  }

  return (
    <div className="flex h-screen w-screen bg-mars-background text-slate-100 font-sans overflow-hidden">
      <Sidebar
        currentView={currentView}
        onNavigate={(viewId) => setCurrentView(viewId)}
        onLogout={() => setCurrentView("login")}
      />

      <main className="grow p-10 flex flex-col h-full min-w-0 gap-8 box-border">
        {currentView === "dashboard" ? (
          <>
            <div className="grid grid-cols-2 gap-8 w-full shrink-0">
              <HeaderCard
                icon={Users}
                title="ZAŁOGA"
                value={`${CREW_MEMBERS_NUMBER} OSOBY`}
              />
              <HeaderCard
                icon={Clock}
                title="CZAS TRWANIA MISJI"
                value={`${MISSION_DURATION} SOL`}
              />
            </div>

            <UsageChart />

            <div className="grid grid-cols-3 gap-8 w-full shrink-0">
              <DetailCard title="ZASOBY POCZĄTKOWE" items={resourceData} />
              <DetailCard title="MODUŁY" items={moduleData} />
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
    </div>
  );
};

export default App;
