import React, { useState } from "react";
import { Trash2, Check, ChevronDown, AlertCircle, X } from "lucide-react";
import type {
  EventData,
  EventEffect,
  EventType,
} from "../../../core/domain/entities/event.ts";
import type { ModuleData } from "../../../core/domain/entities/module.ts";

interface AddEventStepProps {
  mode?: "add" | "edit";
  initialData?: EventData;
  modules: ModuleData[];
  onCancel: () => void;
  onSave: (data: EventData) => void;
}

export const AddEventStep: React.FC<AddEventStepProps> = ({
  mode = "add",
  initialData,
  modules,
  onCancel,
  onSave,
}) => {
  const [name, setName] = useState(initialData?.name || "");
  const [type, setType] = useState<EventType | "">(initialData?.type || "");
  const [duration, setDuration] = useState(
    initialData?.duration ? String(initialData.duration) : "",
  );
  const [effects, setEffects] = useState<EventEffect[]>(
    initialData?.effects || [],
  );

  const [errors, setErrors] = useState({
    name: false,
    type: false,
    duration: false,
  });
  const [isImpactModalOpen, setIsImpactModalOpen] = useState(false);

  const [impactTarget, setImpactTarget] = useState<string>("");
  const [impactAction, setImpactAction] = useState<string>("");
  const [impactValue, setImpactValue] = useState<string>("");
  const [impactErrors, setImpactErrors] = useState({
    target: false,
    action: false,
    value: false,
  });

  const handleBlockNonIntegers = (e: React.KeyboardEvent<HTMLInputElement>) => {
    if (["e", "E", "+", "-", ".", ","].includes(e.key)) e.preventDefault();
  };

  const handleSaveEvent = () => {
    const isNameEmpty = !name.trim();
    const isTypeEmpty = !type;
    const parsedDuration = parseInt(duration, 10);
    const isDurationInvalid =
      !duration || isNaN(parsedDuration) || parsedDuration <= 0;

    if (isNameEmpty || isTypeEmpty || isDurationInvalid) {
      setErrors({
        name: isNameEmpty,
        type: isTypeEmpty,
        duration: isDurationInvalid,
      });
      return;
    }

    onSave({
      id: initialData?.id || crypto.randomUUID(),
      name: name.trim(),
      type: type as EventType,
      duration: parsedDuration,
      effects,
    });
  };

  const openImpactModal = () => {
    setImpactErrors({ target: false, action: false, value: false });
    setImpactTarget("");
    setImpactAction("");
    setImpactValue("");
    setIsImpactModalOpen(true);
  };

  const saveImpact = () => {
    const parsedValue = parseFloat(impactValue.replace(",", ".")) || 0;
    if (!impactTarget || !impactAction || parsedValue <= 0) {
      setImpactErrors({
        target: !impactTarget,
        action: !impactAction,
        value: parsedValue <= 0,
      });
      return;
    }

    const newEffect: EventEffect = {
      target: impactTarget,
      value: parsedValue,
      unit: impactAction === "efficiency" ? "%" : "UNITS",
      description:
        impactAction === "minus"
          ? "DECREASE"
          : impactAction === "plus"
            ? "INCREASE"
            : "EFFICIENCY",
    };

    setEffects([...effects, newEffect]);
    setIsImpactModalOpen(false);
  };

  const getTargetName = (targetId: string) => {
    if (targetId.startsWith("MODULE_")) {
      const mId = targetId.replace("MODULE_", "");
      return modules.find((m) => m.id === mId)?.name || "Moduł";
    }
    const resNames: Record<string, string> = {
      WATER: "Woda",
      OXYGEN: "Tlen",
      FOOD: "Żywność",
      ENERGY: "Energia",
    };
    return resNames[targetId.replace("RESOURCE_", "")] || targetId;
  };

  return (
    <div className="w-full flex flex-col lg:flex-row gap-8 relative">
      <div className="bg-mars-itemBackground py-12 px-4 rounded-4xl shadow-xl w-full flex flex-col justify-between min-h-160">
        <div>
          <h2 className="text-center font-bold tracking-widest text-sm uppercase mb-12 text-mars-orange">
            {mode === "edit" ? "Edycja Zdarzenia" : "Dodawanie Zdarzenia"}
          </h2>
          <div className="flex flex-col gap-14 w-full mt-30">
            <div className="grid grid-cols-[160px_1fr] items-center gap-6">
              <label className="text-[10px] md:text-xs tracking-widest uppercase text-slate-200 text-center">
                Nazwa
              </label>
              <div className="relative w-full pr-4">
                <input
                  type="text"
                  value={name}
                  onChange={(e) => {
                    setName(e.target.value);
                    if (errors.name) setErrors((p) => ({ ...p, name: false }));
                  }}
                  className={`w-full bg-mars-line text-white px-4 py-3 rounded-xl text-center text-sm tracking-wide focus:outline-none transition-all shadow-inner ${errors.name ? "ring-1 ring-red-500 border-red-500" : "focus:ring-1 focus:ring-mars-orange/40"}`}
                />
              </div>
            </div>

            <div className="grid grid-cols-[160px_1fr] items-center gap-6 relative">
              <label className="text-[10px] md:text-xs tracking-widest uppercase text-slate-200 text-center">
                Typ
              </label>
              <div className="relative w-full pr-4">
                <select
                  value={type}
                  onChange={(e) => {
                    setType(e.target.value as EventType);
                    if (errors.type) setErrors((p) => ({ ...p, type: false }));
                  }}
                  className={`w-full bg-mars-line text-white px-4 py-3 rounded-xl text-center text-sm tracking-wide focus:outline-none transition-all shadow-inner appearance-none cursor-pointer ${errors.type ? "ring-1 ring-red-500 border-red-500" : "focus:ring-1 focus:ring-mars-orange/40"}`}
                >
                  <option value="" disabled>
                    Wybierz typ...
                  </option>
                  <option value="THREAT">Zagrożenie / Awaria</option>
                  <option value="SUPPLY_DELIVERY">Dostawa Zapasów</option>
                  <option value="MODULE_STATE_CHANGE">
                    Zmiana Stanu Modułu
                  </option>
                </select>
                <ChevronDown
                  size={18}
                  strokeWidth={3}
                  className="absolute right-8 top-1/2 -translate-y-1/2 text-mars-orange pointer-events-none"
                />
              </div>
            </div>

            <div className="grid grid-cols-[160px_1fr] items-center gap-6">
              <label className="text-[10px] md:text-xs tracking-widest uppercase text-slate-200 text-center">
                Czas Trwania (Sol)
              </label>
              <div className="relative w-full pr-4">
                <input
                  type="number"
                  min="1"
                  step="1"
                  value={duration}
                  onKeyDown={handleBlockNonIntegers}
                  onChange={(e) => {
                    setDuration(e.target.value);
                    if (errors.duration)
                      setErrors((p) => ({ ...p, duration: false }));
                  }}
                  className={`w-full bg-mars-line text-white px-4 py-3 rounded-xl text-center text-sm tracking-wide focus:outline-none transition-all shadow-inner ${errors.duration ? "ring-1 ring-red-500 border-red-500" : "focus:ring-1 focus:ring-mars-orange/40"}`}
                />
              </div>
            </div>
          </div>
        </div>

        <div className="mt-auto flex justify-between w-full px-2 pt-8">
          <button
            onClick={onCancel}
            className="text-red-500 hover:text-red-400 p-2 transition-all active:scale-90 cursor-pointer"
          >
            <Trash2 size={28} strokeWidth={2.5} />
          </button>
          <button
            onClick={handleSaveEvent}
            className="text-green-500 hover:text-green-400 p-2 transition-all active:scale-90 cursor-pointer"
          >
            <Check size={32} strokeWidth={3} />
          </button>
        </div>
      </div>

      <div className="bg-mars-itemBackground px-6 py-12 rounded-4xl shadow-xl w-full flex flex-col h-160">
        <h2 className="text-center font-bold tracking-widest text-sm uppercase mb-10 text-mars-orange">
          Wpływ (Efekty)
        </h2>
        <div className="flex justify-center w-full mb-8">
          <button
            onClick={openImpactModal}
            className="text-[10px] md:text-xs text-slate-300 hover:text-white tracking-widest uppercase transition-colors cursor-pointer"
          >
            Dodaj Efekt
          </button>
        </div>

        <div className="bg-mars-line rounded-xl p-2 grow overflow-y-auto shadow-inner border border-mars-line/30 w-full mb-8">
          <ul className="flex flex-col">
            {effects.length === 0 ? (
              <div className="flex items-center justify-center h-full text-slate-500 text-xs tracking-widest uppercase py-10">
                Brak przypisanych skutków
              </div>
            ) : (
              effects.map((eff, idx) => (
                <li
                  key={idx}
                  className="relative py-3 px-4 flex justify-between items-center rounded-lg mb-1 bg-white/5"
                >
                  <span className="text-[10px] md:text-xs tracking-widest">
                    {getTargetName(eff.target)}
                  </span>
                  <span className="text-[10px] md:text-xs tracking-widest opacity-80">
                    {eff.description} {eff.value}
                    {eff.unit}
                  </span>
                  <button
                    onClick={() =>
                      setEffects(effects.filter((_, i) => i !== idx))
                    }
                    className="text-red-400 hover:text-red-300 ml-4"
                  >
                    <X size={16} />
                  </button>
                </li>
              ))
            )}
          </ul>
        </div>
      </div>

      {isImpactModalOpen && (
        <div className="absolute inset-0 z-50 flex items-center justify-center bg-mars-background/80 backdrop-blur-sm p-6">
          <div className="bg-mars-itemBackground border border-mars-orange/30 p-10 rounded-4xl shadow-2xl flex flex-col w-full max-w-2xl">
            <h2 className="text-center font-bold tracking-widest text-sm uppercase mb-12 text-mars-orange">
              Dodawanie Wpływu
            </h2>
            <div className="grid grid-cols-3 gap-6 items-end w-full">
              <div className="flex flex-col gap-4 relative">
                <label className="text-[10px] md:text-xs tracking-widest uppercase text-slate-200 text-center">
                  Dotyczy
                </label>
                <select
                  value={impactTarget}
                  onChange={(e) => {
                    setImpactTarget(e.target.value);
                    setImpactAction("");
                    setImpactValue("");
                  }}
                  className="w-full bg-mars-line text-white px-4 py-3 rounded-xl text-center text-xs tracking-wide focus:outline-none focus:ring-1 focus:ring-mars-orange/40 appearance-none cursor-pointer"
                >
                  <option value="" disabled>
                    Wybierz...
                  </option>
                  <optgroup label="Zasoby">
                    <option value="RESOURCE_WATER">Woda</option>
                    <option value="RESOURCE_OXYGEN">Tlen</option>
                    <option value="RESOURCE_FOOD">Żywność</option>
                    <option value="RESOURCE_ENERGY">Energia</option>
                  </optgroup>
                  <optgroup label="Moduły">
                    {modules.map((m) => (
                      <option key={m.id} value={`MODULE_${m.id}`}>
                        {m.name}
                      </option>
                    ))}
                  </optgroup>
                </select>
              </div>
              <div className="flex flex-col gap-4 relative">
                <label className="text-[10px] md:text-xs tracking-widest uppercase text-slate-200 text-center">
                  Akcja
                </label>
                <select
                  value={impactAction}
                  onChange={(e) => setImpactAction(e.target.value)}
                  disabled={!impactTarget}
                  className="w-full bg-mars-line text-white px-4 py-3 rounded-xl text-center text-xs tracking-wide focus:outline-none focus:ring-1 focus:ring-mars-orange/40 appearance-none cursor-pointer disabled:opacity-50"
                >
                  <option value="" disabled>
                    Wybierz...
                  </option>
                  {impactTarget.startsWith("MODULE") && (
                    <option value="efficiency">Wydajność (%)</option>
                  )}
                  {impactTarget.startsWith("RESOURCE") && (
                    <>
                      <option value="plus">Dodaj Zasób</option>
                      <option value="minus">Odbierz Zasób</option>
                    </>
                  )}
                </select>
              </div>
              <div className="flex flex-col gap-4 relative">
                <label className="text-[10px] md:text-xs tracking-widest uppercase text-slate-200 text-center">
                  Wartość
                </label>
                <input
                  type="number"
                  min="0"
                  step="0.1"
                  value={impactValue}
                  onChange={(e) => setImpactValue(e.target.value)}
                  disabled={!impactAction}
                  className="w-full bg-mars-line text-white px-4 py-3 rounded-xl text-center text-xs tracking-wide focus:outline-none focus:ring-1 focus:ring-mars-orange/40 disabled:opacity-50"
                />
              </div>
            </div>
            {(impactErrors.target ||
              impactErrors.action ||
              impactErrors.value) && (
              <div className="w-full text-center mt-4 text-red-500 text-xs tracking-widest uppercase font-bold">
                Wypełnij wszystkie pola
              </div>
            )}
            <div className="mt-12 flex justify-between w-full px-4">
              <button
                onClick={() => setIsImpactModalOpen(false)}
                className="text-red-500 hover:text-red-400"
              >
                <Trash2 size={24} strokeWidth={2.5} />
              </button>
              <button
                onClick={saveImpact}
                className="text-green-500 hover:text-green-400"
              >
                <Check size={28} strokeWidth={3} />
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
