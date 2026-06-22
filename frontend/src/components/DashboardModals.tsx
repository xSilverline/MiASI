import React, { useState } from "react";
import { Check, X } from "lucide-react";
import type {
  CrewData,
  ResourcesData,
  ModuleWithCount,
} from "../infrastructure/MissionAdapter";

const handleBlockNonIntegers = (e: React.KeyboardEvent<HTMLInputElement>) => {
  if (["e", "E", "+", "-", ".", ","].includes(e.key)) e.preventDefault();
};

export const CrewModal = ({
  data,
  onClose,
  onSave,
}: {
  data: CrewData;
  onClose: () => void;
  onSave: (d: CrewData) => void;
}) => {
  const [edit, setEdit] = useState(data);

  return (
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
              step="1"
              value={edit.men}
              onKeyDown={handleBlockNonIntegers}
              onChange={(e) =>
                setEdit({ ...edit, men: parseInt(e.target.value, 10) || 0 })
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
              step="1"
              value={edit.women}
              onKeyDown={handleBlockNonIntegers}
              onChange={(e) =>
                setEdit({ ...edit, women: parseInt(e.target.value, 10) || 0 })
              }
              className="w-full bg-mars-line text-white px-4 py-2.5 rounded-xl text-center text-sm focus:outline-none focus:ring-1 focus:ring-mars-orange/40"
            />
          </div>
        </div>
        <div className="flex w-full gap-4">
          <button
            onClick={onClose}
            className="flex-1 py-3 flex justify-center rounded-xl border border-mars-line text-slate-300 hover:bg-mars-line/50 hover:text-red-400 transition-colors cursor-pointer"
          >
            <X size={24} />
          </button>
          <button
            onClick={() => {
              if (JSON.stringify(edit) !== JSON.stringify(data)) onSave(edit);
              onClose();
            }}
            className="flex-1 py-3 flex justify-center rounded-xl bg-green-500/10 text-green-500 border border-green-500/30 hover:bg-green-500/20 transition-colors cursor-pointer"
          >
            <Check size={24} />
          </button>
        </div>
      </div>
    </div>
  );
};

export const ResourcesModal = ({
  data,
  onClose,
  onSave,
}: {
  data: ResourcesData;
  onClose: () => void;
  onSave: (d: ResourcesData) => void;
}) => {
  const [edit, setEdit] = useState(data);

  return (
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
                step="1"
                value={edit[key as keyof ResourcesData]}
                onKeyDown={handleBlockNonIntegers}
                onChange={(e) =>
                  setEdit({ ...edit, [key]: parseInt(e.target.value, 10) || 0 })
                }
                className="w-full bg-mars-line text-white px-4 py-2.5 rounded-xl text-center text-sm focus:outline-none focus:ring-1 focus:ring-mars-orange/40"
              />
            </div>
          ))}
        </div>
        <div className="flex w-full gap-4">
          <button
            onClick={onClose}
            className="flex-1 py-3 flex justify-center rounded-xl border border-mars-line text-slate-300 hover:bg-mars-line/50 hover:text-red-400 transition-colors cursor-pointer"
          >
            <X size={24} />
          </button>
          <button
            onClick={() => {
              if (JSON.stringify(edit) !== JSON.stringify(data)) onSave(edit);
              onClose();
            }}
            className="flex-1 py-3 flex justify-center rounded-xl bg-green-500/10 text-green-500 border border-green-500/30 hover:bg-green-500/20 transition-colors cursor-pointer"
          >
            <Check size={24} />
          </button>
        </div>
      </div>
    </div>
  );
};

export const ModulesModal = ({
  data,
  onClose,
  onSave,
}: {
  data: ModuleWithCount[];
  onClose: () => void;
  onSave: (d: ModuleWithCount[]) => void;
}) => {
  const [edit, setEdit] = useState(data);

  return (
    <div className="absolute inset-0 z-50 flex items-center justify-center bg-mars-background/80 backdrop-blur-sm p-6">
      <div className="bg-mars-itemBackground border border-mars-line p-10 rounded-3xl shadow-2xl flex flex-col items-center max-w-md w-full animate-in fade-in zoom-in duration-300">
        <h3 className="text-sm md:text-base font-bold tracking-widest text-mars-orange uppercase mb-8">
          Zestawienie Modułów
        </h3>
        <div className="flex flex-col w-full gap-3 mb-10 max-h-[50vh] overflow-y-auto pr-2">
          {edit.map((mod) => (
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
                step="1"
                value={mod.count}
                onKeyDown={handleBlockNonIntegers}
                onChange={(e) => {
                  const val = parseInt(e.target.value, 10) || 0;
                  setEdit((prev) =>
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
            onClick={onClose}
            className="flex-1 py-3 flex justify-center rounded-xl border border-mars-line text-slate-300 hover:bg-mars-line/50 hover:text-red-400 transition-colors cursor-pointer"
          >
            <X size={24} />
          </button>
          <button
            onClick={() => {
              if (JSON.stringify(edit) !== JSON.stringify(data)) onSave(edit);
              onClose();
            }}
            className="flex-1 py-3 flex justify-center rounded-xl bg-green-500/10 text-green-500 border border-green-500/30 hover:bg-green-500/20 transition-colors cursor-pointer"
          >
            <Check size={24} />
          </button>
        </div>
      </div>
    </div>
  );
};
