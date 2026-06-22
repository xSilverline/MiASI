import React, { useState } from "react";
import { ChevronRight, Check, X } from "lucide-react";
import { DoubleConfigRow } from "../../components/ConfigRow.tsx";
import type { ResourceConsumption } from "../../../core/domain/value-objects/ResourceConsuption.ts";

interface ResourceConfigStepProps {
  initialData?: ResourceConsumption;
  onNext: (data: ResourceConsumption) => void;
  standaloneMode?: boolean;
  onCancel?: () => void;
}

type LocalConsumptionValues = { opt: string; min: string };
type LocalResourceConsumption = Record<
  keyof ResourceConsumption,
  LocalConsumptionValues
>;

export const ResourceConfigStep: React.FC<ResourceConfigStepProps> = ({
  initialData,
  onNext,
  standaloneMode = false,
  onCancel,
}) => {
  const [consumption, setConsumption] = useState<LocalResourceConsumption>(
    () => {
      if (initialData) {
        return {
          maleFood: {
            opt: String(initialData.maleFood.opt),
            min: String(initialData.maleFood.min),
          },
          femaleFood: {
            opt: String(initialData.femaleFood.opt),
            min: String(initialData.femaleFood.min),
          },
          oxygen: {
            opt: String(initialData.oxygen.opt),
            min: String(initialData.oxygen.min),
          },
          water: {
            opt: String(initialData.water.opt),
            min: String(initialData.water.min),
          },
        };
      }
      return {
        maleFood: { opt: "0", min: "0" },
        femaleFood: { opt: "0", min: "0" },
        oxygen: { opt: "0", min: "0" },
        water: { opt: "0", min: "0" },
      };
    },
  );

  const handleChange = (
    key: keyof LocalResourceConsumption,
    field: "opt" | "min",
    val: string,
  ) => {
    setConsumption((prev) => ({
      ...prev,
      [key]: { ...prev[key], [field]: val },
    }));
  };

  const handleSave = () => {
    const parsedData: ResourceConsumption = {
      maleFood: {
        opt: parseFloat(consumption.maleFood.opt.replace(",", ".")) || 0,
        min: parseFloat(consumption.maleFood.min.replace(",", ".")) || 0,
      },
      femaleFood: {
        opt: parseFloat(consumption.femaleFood.opt.replace(",", ".")) || 0,
        min: parseFloat(consumption.femaleFood.min.replace(",", ".")) || 0,
      },
      oxygen: {
        opt: parseFloat(consumption.oxygen.opt.replace(",", ".")) || 0,
        min: parseFloat(consumption.oxygen.min.replace(",", ".")) || 0,
      },
      water: {
        opt: parseFloat(consumption.water.opt.replace(",", ".")) || 0,
        min: parseFloat(consumption.water.min.replace(",", ".")) || 0,
      },
    };
    onNext(parsedData);
  };

  return (
    <div className="bg-mars-itemBackground py-12 px-6 rounded-4xl shadow-xl w-full flex flex-col h-[640px] relative">
      <h2 className="text-center font-bold tracking-widest text-sm uppercase mb-8 text-mars-orange">
        Zużycie Zasobów
      </h2>

      <div className="flex flex-col gap-6 w-full">
        <DoubleConfigRow
          label={
            <>
              Zużycie Racji
              <br />
              Żyw. (Mężczyzna)
            </>
          }
          valueOpt={consumption.maleFood.opt}
          valueMin={consumption.maleFood.min}
          onChangeOpt={(val) => handleChange("maleFood", "opt", val)}
          onChangeMin={(val) => handleChange("maleFood", "min", val)}
          unit="Porcji"
        />
        <DoubleConfigRow
          label={
            <>
              Zużycie Racji
              <br />
              Żyw. (Kobieta)
            </>
          }
          valueOpt={consumption.femaleFood.opt}
          valueMin={consumption.femaleFood.min}
          onChangeOpt={(val) => handleChange("femaleFood", "opt", val)}
          onChangeMin={(val) => handleChange("femaleFood", "min", val)}
          unit="Porcji"
        />
        <DoubleConfigRow
          label="Tlen / Sol"
          valueOpt={consumption.oxygen.opt}
          valueMin={consumption.oxygen.min}
          onChangeOpt={(val) => handleChange("oxygen", "opt", val)}
          onChangeMin={(val) => handleChange("oxygen", "min", val)}
          unit="Litrów"
        />
        <DoubleConfigRow
          label="Woda / Sol"
          valueOpt={consumption.water.opt}
          valueMin={consumption.water.min}
          onChangeOpt={(val) => handleChange("water", "opt", val)}
          onChangeMin={(val) => handleChange("water", "min", val)}
          unit="Litrów"
        />
      </div>

      <div
        className={`mt-auto flex w-full px-2 ${standaloneMode ? "justify-between" : "justify-end"}`}
      >
        {standaloneMode ? (
          <>
            <button
              onClick={onCancel}
              className="text-red-500 hover:text-red-400 p-2 transition-all active:scale-95 cursor-pointer"
            >
              <X size={32} strokeWidth={3} />
            </button>
            <button
              onClick={handleSave}
              className="text-green-500 hover:text-green-400 p-2 transition-all active:scale-95 cursor-pointer"
            >
              <Check size={32} strokeWidth={3} />
            </button>
          </>
        ) : (
          <button
            onClick={handleSave}
            className="bg-mars-orange hover:bg-mars-orange/90 text-mars-background p-2.5 rounded-xl transition-all shadow-md active:scale-95 cursor-pointer"
          >
            <ChevronRight size={24} strokeWidth={3} />
          </button>
        )}
      </div>
    </div>
  );
};
