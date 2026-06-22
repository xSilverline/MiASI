import React, { useState } from "react";
import { Trash2, Check, ChevronDown, AlertCircle } from "lucide-react";
import type { ResourceKey, ModuleData } from "../../types/module";

interface AddModuleStepProps {
  mode?: "add" | "edit";
  initialData?: ModuleData;
  onCancel: () => void;
  onSave: (data: Partial<ModuleData>) => void;
}

export const AddModuleStep: React.FC<AddModuleStepProps> = ({
  mode = "add",
  initialData,
  onCancel,
  onSave,
}) => {
  const [name, setName] = useState(initialData?.name || "");
  const [type, setType] = useState(initialData?.type || "");
  const [errors, setErrors] = useState({ name: false, type: false });

  const [resources, setResources] = useState<
    Record<ResourceKey, { prod: string; cons: string }>
  >(() => {
    if (initialData) {
      return {
        woda: {
          prod: String(initialData.resources.woda.prod),
          cons: String(initialData.resources.woda.cons),
        },
        tlen: {
          prod: String(initialData.resources.tlen.prod),
          cons: String(initialData.resources.tlen.cons),
        },
        zywnosc: {
          prod: String(initialData.resources.zywnosc.prod),
          cons: String(initialData.resources.zywnosc.cons),
        },
        energia: {
          prod: String(initialData.resources.energia.prod),
          cons: String(initialData.resources.energia.cons),
        },
      };
    }
    return {
      woda: { prod: "0", cons: "0" },
      tlen: { prod: "0", cons: "0" },
      zywnosc: { prod: "0", cons: "0" },
      energia: { prod: "0", cons: "0" },
    };
  });

  const handleBlockInvalidFloats = (
    e: React.KeyboardEvent<HTMLInputElement>,
  ) => {
    if (["e", "E", "+", "-"].includes(e.key)) e.preventDefault();
  };

  const handleResourceChange = (
    res: ResourceKey,
    field: "prod" | "cons",
    val: string,
  ) => {
    setResources((prev) => ({
      ...prev,
      [res]: { ...prev[res], [field]: val },
    }));
  };

  const handleSave = () => {
    const isNameEmpty = !name.trim();
    const isTypeEmpty = !type;

    if (isNameEmpty || isTypeEmpty) {
      setErrors({ name: isNameEmpty, type: isTypeEmpty });
      return;
    }

    const parseRes = (val: string) => parseFloat(val.replace(",", ".")) || 0;

    const parsedResources = {
      woda: {
        prod: parseRes(resources.woda.prod),
        cons: parseRes(resources.woda.cons),
      },
      tlen: {
        prod: parseRes(resources.tlen.prod),
        cons: parseRes(resources.tlen.cons),
      },
      zywnosc: {
        prod: parseRes(resources.zywnosc.prod),
        cons: parseRes(resources.zywnosc.cons),
      },
      energia: {
        prod: parseRes(resources.energia.prod),
        cons: parseRes(resources.energia.cons),
      },
    };

    onSave({
      id: initialData?.id,
      name: name.trim(),
      type,
      resources: parsedResources,
    });
  };

  const resourceTypes: { id: ResourceKey; label: string }[] = [
    { id: "woda", label: "Woda" },
    { id: "tlen", label: "Tlen" },
    { id: "zywnosc", label: "Żywność" },
    { id: "energia", label: "Energia" },
  ];

  return (
    <div className="bg-mars-itemBackground p-10 md:p-14 rounded-4xl shadow-xl w-full flex flex-col relative">
      <h2 className="text-center font-bold tracking-widest text-sm uppercase mb-12 text-slate-100">
        {mode === "edit" ? "Edycja Modułu" : "Dodawanie Modułu"}
      </h2>

      <div className="flex flex-col gap-5 w-full max-w-sm mx-auto mb-10">
        <div className="grid grid-cols-[80px_1fr] items-center gap-6">
          <label className="text-[10px] md:text-xs tracking-widest uppercase text-slate-200 text-right">
            Nazwa
          </label>
          <div className="relative w-full">
            <input
              type="text"
              value={name}
              onChange={(e) => {
                setName(e.target.value);
                if (errors.name)
                  setErrors((prev) => ({ ...prev, name: false }));
              }}
              placeholder="np. Farma"
              className={`w-full bg-mars-line text-white px-4 py-3 rounded-xl text-center text-sm tracking-wide focus:outline-none transition-all font-medium shadow-inner ${
                errors.name
                  ? "ring-1 ring-red-500 border border-red-500"
                  : "focus:ring-1 focus:ring-mars-orange/40"
              }`}
            />
            {errors.name && (
              <div className="absolute right-4 top-1/2 -translate-y-1/2 text-red-500 pointer-events-none">
                <AlertCircle size={18} strokeWidth={2.5} />
              </div>
            )}
          </div>
        </div>

        <div className="grid grid-cols-[80px_1fr] items-center gap-6 relative">
          <label className="text-[10px] md:text-xs tracking-widest uppercase text-slate-200 text-right">
            Typ
          </label>
          <div className="relative w-full">
            <select
              value={type}
              onChange={(e) => {
                setType(e.target.value);
                if (errors.type)
                  setErrors((prev) => ({ ...prev, type: false }));
              }}
              className={`w-full bg-mars-line text-white px-4 py-3 rounded-xl text-center text-sm tracking-wide focus:outline-none transition-all font-medium shadow-inner appearance-none cursor-pointer ${
                errors.type
                  ? "ring-1 ring-red-500 border border-red-500"
                  : "focus:ring-1 focus:ring-mars-orange/40"
              }`}
            >
              <option value="" disabled>
                Wybierz typ...
              </option>
              <option value="mieszkalny">Mieszkalny</option>
              <option value="produkcyjny">Produkcyjny</option>
              <option value="energetyczny">Energetyczny</option>
              <option value="magazynowy">Magazynowy</option>
              <option value="uzytkowy">Użytkowy</option>
            </select>
            <div className="absolute right-4 top-1/2 -translate-y-1/2 pointer-events-none flex items-center gap-2">
              {errors.type && (
                <AlertCircle
                  size={18}
                  strokeWidth={2.5}
                  className="text-red-500"
                />
              )}
              <ChevronDown
                size={18}
                strokeWidth={3}
                className="text-mars-orange"
              />
            </div>
          </div>
        </div>
      </div>

      <div className="w-full max-w-md mx-auto">
        <div className="grid grid-cols-[80px_1fr_1fr] gap-6 mb-4 text-[10px] md:text-xs tracking-widest uppercase text-slate-200 text-center font-bold">
          <div>Zasób</div>
          <div>Produkcja</div>
          <div>Zużycie</div>
        </div>

        {resourceTypes.map((res) => (
          <div
            key={res.id}
            className="grid grid-cols-[80px_1fr_1fr] gap-6 items-center mb-3"
          >
            <div className="text-[10px] md:text-xs tracking-widest uppercase text-slate-200 text-center font-medium">
              {res.label}
            </div>
            <input
              type="number"
              min="0"
              step="0.1"
              value={resources[res.id].prod}
              onKeyDown={handleBlockInvalidFloats}
              onChange={(e) =>
                handleResourceChange(res.id, "prod", e.target.value)
              }
              className="w-full bg-mars-line text-white px-2 py-2.5 rounded-xl text-center text-sm tracking-wide focus:outline-none focus:ring-1 focus:ring-mars-orange/40 transition-all font-medium shadow-inner"
            />
            <input
              type="number"
              min="0"
              step="0.1"
              value={resources[res.id].cons}
              onKeyDown={handleBlockInvalidFloats}
              onChange={(e) =>
                handleResourceChange(res.id, "cons", e.target.value)
              }
              className="w-full bg-mars-line text-white px-2 py-2.5 rounded-xl text-center text-sm tracking-wide focus:outline-none focus:ring-1 focus:ring-mars-orange/40 transition-all font-medium shadow-inner"
            />
          </div>
        ))}
      </div>

      <div className="mt-12 flex justify-between w-full px-2">
        <button
          onClick={onCancel}
          className="text-red-500 hover:text-red-400 p-2 transition-all active:scale-90 cursor-pointer"
        >
          <Trash2 size={28} strokeWidth={2.5} />
        </button>
        <button
          onClick={handleSave}
          className="text-green-500 hover:text-green-400 p-2 transition-all active:scale-90 cursor-pointer"
        >
          <Check size={32} strokeWidth={3} />
        </button>
      </div>
    </div>
  );
};
