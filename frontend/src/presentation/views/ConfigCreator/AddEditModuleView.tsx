import React, { useState } from "react";
import { Trash2, Check, ChevronDown, AlertCircle } from "lucide-react";
import type {
  ModuleData,
  ResourceType,
  ModuleStatus,
  ModuleCategory,
  ResourceQuantity,
} from "../../../core/domain/entities/module.ts";

interface AddModuleStepProps {
  mode?: "add" | "edit";
  initialData?: ModuleData;
  onCancel: () => void;
  onSave: (data: ModuleData) => void;
}

export const AddModuleStep: React.FC<AddModuleStepProps> = ({
  mode = "add",
  initialData,
  onCancel,
  onSave,
}) => {
  const [name, setName] = useState(initialData?.name || "");
  const [category, setCategory] = useState<ModuleCategory | "">(
    initialData?.category || "",
  );
  const [errors, setErrors] = useState({ name: false, category: false });
  const [status, setStatus] = useState<ModuleStatus>(
    initialData?.status || "ACTIVE",
  );
  const [weight, setWeight] = useState(
    initialData?.weight ? String(initialData.weight) : "0",
  );

  const [resources, setResources] = useState<
    Record<ResourceType, { prod: string; cons: string }>
  >(() => {
    const getRes = (arr: ResourceQuantity[] | undefined, type: ResourceType) =>
      arr?.find((r) => r.resourceType === type)?.quantity || 0;

    return {
      WATER: {
        prod: String(getRes(initialData?.resourceProduction, "WATER")),
        cons: String(getRes(initialData?.resourceConsumption, "WATER")),
      },
      OXYGEN: {
        prod: String(getRes(initialData?.resourceProduction, "OXYGEN")),
        cons: String(getRes(initialData?.resourceConsumption, "OXYGEN")),
      },
      FOOD: {
        prod: String(getRes(initialData?.resourceProduction, "FOOD")),
        cons: String(getRes(initialData?.resourceConsumption, "FOOD")),
      },
      ENERGY: {
        prod: String(getRes(initialData?.resourceProduction, "ENERGY")),
        cons: String(getRes(initialData?.resourceConsumption, "ENERGY")),
      },
    };
  });

  const handleBlockInvalidFloats = (
    e: React.KeyboardEvent<HTMLInputElement>,
  ) => {
    if (["e", "E", "+", "-"].includes(e.key)) e.preventDefault();
  };

  const handleResourceChange = (
    res: ResourceType,
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
    const isCategoryEmpty = !category;

    if (isNameEmpty || isCategoryEmpty) {
      setErrors({ name: isNameEmpty, category: isCategoryEmpty });
      return;
    }

    const parseRes = (val: string) => parseFloat(val.replace(",", ".")) || 0;

    const resourceProduction: ResourceQuantity[] = [];
    const resourceConsumption: ResourceQuantity[] = [];

    (Object.keys(resources) as ResourceType[]).forEach((type) => {
      const prodVal = parseRes(resources[type].prod);
      const consVal = parseRes(resources[type].cons);
      if (prodVal > 0)
        resourceProduction.push({ resourceType: type, quantity: prodVal });
      if (consVal > 0)
        resourceConsumption.push({ resourceType: type, quantity: consVal });
    });

    onSave({
      id: initialData?.id || crypto.randomUUID(),
      name: name.trim(),
      category: category as ModuleCategory,
      status,
      weight: parseFloat(weight.replace(",", ".")) || 0,
      resourceProduction,
      resourceConsumption,
    });
  };

  const resourceTypes: { id: ResourceType; label: string }[] = [
    { id: "WATER", label: "Woda" },
    { id: "OXYGEN", label: "Tlen" },
    { id: "FOOD", label: "Żywność" },
    { id: "ENERGY", label: "Energia" },
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
              <AlertCircle
                size={18}
                strokeWidth={2.5}
                className="absolute right-4 top-1/2 -translate-y-1/2 text-red-500 pointer-events-none"
              />
            )}
          </div>
        </div>

        <div className="grid grid-cols-[80px_1fr] items-center gap-6 relative">
          <label className="text-[10px] md:text-xs tracking-widest uppercase text-slate-200 text-right">
            Kategoria
          </label>
          <div className="relative w-full">
            <select
              value={category}
              onChange={(e) => {
                setCategory(e.target.value as ModuleCategory);
                if (errors.category)
                  setErrors((prev) => ({ ...prev, category: false }));
              }}
              className={`w-full bg-mars-line text-white px-4 py-3 rounded-xl text-center text-sm tracking-wide focus:outline-none transition-all font-medium shadow-inner appearance-none cursor-pointer ${
                errors.category
                  ? "ring-1 ring-red-500 border border-red-500"
                  : "focus:ring-1 focus:ring-mars-orange/40"
              }`}
            >
              <option value="" disabled>
                Wybierz kategorię...
              </option>
              <option value="UTILITY_MODULE">Użytkowy (Utility)</option>
              <option value="ENERGY_MODULE">Energetyczny (Energy)</option>
            </select>
            <ChevronDown
              size={18}
              strokeWidth={3}
              className="absolute right-4 top-1/2 -translate-y-1/2 text-mars-orange pointer-events-none"
            />
          </div>
        </div>

        <div className="grid grid-cols-[80px_1fr] items-center gap-6 relative">
          <label className="text-[10px] md:text-xs tracking-widest uppercase text-slate-200 text-right">
            Status
          </label>
          <div className="relative w-full">
            <select
              value={status}
              onChange={(e) => setStatus(e.target.value as ModuleStatus)}
              className="w-full bg-mars-line text-white px-4 py-3 rounded-xl text-center text-sm tracking-wide focus:outline-none focus:ring-1 focus:ring-mars-orange/40 transition-all font-medium shadow-inner appearance-none cursor-pointer"
            >
              <option value="ACTIVE">AKTYWNY</option>
              <option value="PARTIALLY_DAMAGED">CZĘŚCIOWO USZKODZONY</option>
              <option value="DESTROYED">ZNISZCZONY</option>
              <option value="INACTIVE">NIEAKTYWNY</option>
            </select>
            <ChevronDown
              size={18}
              strokeWidth={3}
              className="absolute right-4 top-1/2 -translate-y-1/2 text-mars-orange pointer-events-none"
            />
          </div>
        </div>

        <div className="grid grid-cols-[80px_1fr] items-center gap-6">
          <label className="text-[10px] md:text-xs tracking-widest uppercase text-slate-200 text-right">
            Waga (kg)
          </label>
          <div className="relative w-full">
            <input
              type="number"
              min="0"
              step="0.1"
              value={weight}
              onKeyDown={handleBlockInvalidFloats}
              onChange={(e) => setWeight(e.target.value)}
              className="w-full bg-mars-line text-white px-4 py-3 rounded-xl text-center text-sm tracking-wide focus:outline-none focus:ring-1 focus:ring-mars-orange/40 transition-all font-medium shadow-inner"
            />
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
              className="w-full bg-mars-line text-white px-2 py-2.5 rounded-xl text-center text-sm tracking-wide focus:outline-none focus:ring-1 focus:ring-mars-orange/40 transition-all shadow-inner"
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
              className="w-full bg-mars-line text-white px-2 py-2.5 rounded-xl text-center text-sm tracking-wide focus:outline-none focus:ring-1 focus:ring-mars-orange/40 transition-all shadow-inner"
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
