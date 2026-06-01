import React from "react";
import { LayoutDashboard, Rocket, Settings, LogOut } from "lucide-react";
import marsIcon from "../assets/mars.png";

const menuGroups = [
  {
    group: "PLANOWANIE",
    icon: LayoutDashboard,
    color: "text-mars-orange",
    items: [
      { text: "PODSUMOWANIE", active: true },
      { text: "EDYCJA PLANU" },
      { text: "HARMONOGRAM MISJI" },
    ],
  },
  {
    group: "SYMULACJA",
    icon: Rocket,
    color: "text-mars-orange",
    items: [{ text: "AUTOMATYCZNA" }, { text: "RĘCZNA" }],
  },
  {
    group: "KONFIGURACJA",
    icon: Settings,
    color: "text-mars-orange",
    items: [
      { text: "ZUŻYCIE ZASOBÓW" },
      { text: "MODUŁY" },
      { text: "ZDARZENIA" },
      { text: "KREATOR MISJI" },
    ],
  },
];
interface SidebarProps {
  onLogout: () => void;
}

export const Sidebar: React.FC<SidebarProps> = ({ onLogout }) => {
  return (
    <aside className=" bg-mars-sidebarBackground py-6 flex flex-col shrink-0 h-full">
      <div className="flex items-center gap-3 mb-10 px-6">
        <img
          src={marsIcon}
          alt="Mars Icon"
          className="w-15 h-15 object-contain shrink-0"
        />
        <div>
          <h1 className="text-3xl font-bold text-gradient-mars tracking-wider">
            MISJA MARS
          </h1>
          <p className="text-base text-slate-200">PANEL ZARZĄDZANIA</p>
        </div>
      </div>

      <nav className="grow space-y-6 overflow-y-auto">
        {menuGroups.map((group, gIdx) => (
          <div key={gIdx}>
            <div className={`flex items-center gap-3 ${group.color} mb-3 px-6`}>
              <group.icon size={20} className="shrink-0" />
              <span className="font-semibold text-base tracking-wide">
                {group.group}
              </span>
            </div>
            <ul className="text-sm text-slate-300 flex flex-col">
              {group.items.map((item, iIdx) => (
                <li
                  key={iIdx}
                  className={`cursor-pointer transition-all duration-200 relative py-3 pl-12 pr-6 flex items-center overflow-hidden
                    ${item.active ? "text-white font-medium" : "hover:text-white hover:bg-white/5"}`}
                >
                  {item.active && (
                    <>
                      <div className="absolute inset-0 bg-linear-to-r from-mars-orange/40 via-mars-orange/10 to-transparent pointer-events-none" />
                      <div
                        className="absolute left-0 top-0 bottom-0 w-4 bg-linear-to-r from-mars-orange to-[#B33C12]"
                        style={{ clipPath: "polygon(0 0, 100% 50%, 0 100%)" }}
                      />
                    </>
                  )}
                  <span className="relative z-10">{item.text}</span>
                </li>
              ))}
            </ul>
          </div>
        ))}
      </nav>

      <div
        onClick={onLogout}
        className="mt-6 border-t border-mars-line pt-4 px-6 text-slate-400 hover:text-white cursor-pointer flex items-center gap-3 text-sm transition-colors"
      >
        <LogOut size={18} />
        WYLOGUJ
      </div>
    </aside>
  );
};
