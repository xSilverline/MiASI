import React, { useState } from "react";
import { ChevronRight, Check, X } from "lucide-react";
import { DoubleConfigRow } from "../../components/ConfigRow.tsx";
import type { SexProfile } from "../../../core/domain/value-objects/ResourceConsuption.ts";
import type { ResourceQuantity } from "../../../core/domain/entities/module.ts";

export interface GeneralConfigData {
  missionDuration: number;
  maxStartingWeight: number;
  crew: SexProfile[];
  startingResources: ResourceQuantity[];
}

interface ResourceConfigStepProps {
  initialData?: Partial<GeneralConfigData>;
  onNext: (data: GeneralConfigData) => void;
  standaloneMode?: boolean;
  onCancel?: () => void;
}

export const ResourceConfigStep: React.FC<ResourceConfigStepProps> = ({
  initialData,
  onNext,
  standaloneMode = false,
  onCancel,
}) => {
  const [missionDuration, setMissionDuration] = useState(
    String(initialData?.missionDuration || 700),
  );
  const [maxStartingWeight, setMaxStartingWeight] = useState(
    String(initialData?.maxStartingWeight || 150000),
  );

  const getStartingRes = (type: string) =>
    initialData?.startingResources?.find((r) => r.resourceType === type)
      ?.quantity || 0;
  const [startRes, setStartRes] = useState({
    oxygen: String(getStartingRes("OXYGEN") || 50000),
    water: String(getStartingRes("WATER") || 30000),
    food: String(getStartingRes("FOOD") || 3100),
  });

  const getProfile = (name: string) =>
    initialData?.crew?.find((c) => c.name === name);
  const maleProfile = getProfile("Male");
  const femaleProfile = getProfile("Female");

  const [consumption, setConsumption] = useState({
    maleFood: {
      opt: String(maleProfile?.optimalDemand?.FOOD || 0),
      min: String(maleProfile?.minimalDemand?.FOOD || 0),
    },
    femaleFood: {
      opt: String(femaleProfile?.optimalDemand?.FOOD || 0),
      min: String(femaleProfile?.minimalDemand?.FOOD || 0),
    },
    oxygen: {
      opt: String(maleProfile?.optimalDemand?.OXYGEN || 0),
      min: String(maleProfile?.minimalDemand?.OXYGEN || 0),
    },
    water: {
      opt: String(maleProfile?.optimalDemand?.WATER || 0),
      min: String(maleProfile?.minimalDemand?.WATER || 0),
    },
  });

  const handleSave = () => {
    const parse = (val: string) => parseFloat(val.replace(",", ".")) || 0;

    const generalData: GeneralConfigData = {
      missionDuration: parseInt(missionDuration) || 700,
      maxStartingWeight: parse(maxStartingWeight),
      startingResources: [
        { resourceType: "OXYGEN", quantity: parse(startRes.oxygen) },
        { resourceType: "WATER", quantity: parse(startRes.water) },
        { resourceType: "FOOD", quantity: parse(startRes.food) },
      ],
      crew: [
        {
          name: "Male",
          population: maleProfile?.population || 2,
          optimalDemand: {
            FOOD: parse(consumption.maleFood.opt),
            WATER: parse(consumption.water.opt),
            OXYGEN: parse(consumption.oxygen.opt),
          },
          minimalDemand: {
            FOOD: parse(consumption.maleFood.min),
            WATER: parse(consumption.water.min),
            OXYGEN: parse(consumption.oxygen.min),
          },
        },
        {
          name: "Female",
          population: femaleProfile?.population || 2,
          optimalDemand: {
            FOOD: parse(consumption.femaleFood.opt),
            WATER: parse(consumption.water.opt),
            OXYGEN: parse(consumption.oxygen.opt),
          },
          minimalDemand: {
            FOOD: parse(consumption.femaleFood.min),
            WATER: parse(consumption.water.min),
            OXYGEN: parse(consumption.oxygen.min),
          },
        },
      ],
    };
    onNext(generalData);
  };

  return (
    <div className="bg-mars-itemBackground py-8 px-6 rounded-4xl shadow-xl w-full flex flex-col h-auto min-h-[640px] relative">
      <h2 className="text-center font-bold tracking-widest text-sm uppercase mb-8 text-mars-orange">
        Parametry Startowe
      </h2>

      <div className="flex flex-col gap-4 w-full mb-6">
        <div className="flex justify-between items-center bg-white/5 py-3 px-4 rounded-xl border border-white/5">
          <label className="text-[10px] md:text-xs font-bold tracking-widest text-slate-200 uppercase">
            Czas (SOL)
          </label>
          <input
            type="number"
            min="1"
            value={missionDuration}
            onChange={(e) => setMissionDuration(e.target.value)}
            className="w-24 bg-mars-line text-white px-3 py-1.5 rounded-lg text-center text-xs focus:outline-none focus:ring-1 focus:ring-mars-orange"
          />
        </div>
        <div className="flex justify-between items-center bg-white/5 py-3 px-4 rounded-xl border border-white/5">
          <label className="text-[10px] md:text-xs font-bold tracking-widest text-slate-200 uppercase">
            Waga (kg)
          </label>
          <input
            type="number"
            min="0"
            value={maxStartingWeight}
            onChange={(e) => setMaxStartingWeight(e.target.value)}
            className="w-24 bg-mars-line text-white px-3 py-1.5 rounded-lg text-center text-xs focus:outline-none focus:ring-1 focus:ring-mars-orange"
          />
        </div>
      </div>

      <h2 className="text-center font-bold tracking-widest text-sm uppercase mb-6 text-mars-orange mt-2">
        Dzienne Zużycie na Osobę
      </h2>
      <div className="flex flex-col gap-4 w-full">
        <DoubleConfigRow
          label={
            <>
              Żywność
              <br />
              (Mężczyzna)
            </>
          }
          valueOpt={consumption.maleFood.opt}
          valueMin={consumption.maleFood.min}
          onChangeOpt={(v) =>
            setConsumption({
              ...consumption,
              maleFood: { ...consumption.maleFood, opt: v },
            })
          }
          onChangeMin={(v) =>
            setConsumption({
              ...consumption,
              maleFood: { ...consumption.maleFood, min: v },
            })
          }
          unit="Porcji"
        />
        <DoubleConfigRow
          label={
            <>
              Żywność
              <br />
              (Kobieta)
            </>
          }
          valueOpt={consumption.femaleFood.opt}
          valueMin={consumption.femaleFood.min}
          onChangeOpt={(v) =>
            setConsumption({
              ...consumption,
              femaleFood: { ...consumption.femaleFood, opt: v },
            })
          }
          onChangeMin={(v) =>
            setConsumption({
              ...consumption,
              femaleFood: { ...consumption.femaleFood, min: v },
            })
          }
          unit="Porcji"
        />
        <DoubleConfigRow
          label="Tlen"
          valueOpt={consumption.oxygen.opt}
          valueMin={consumption.oxygen.min}
          onChangeOpt={(v) =>
            setConsumption({
              ...consumption,
              oxygen: { ...consumption.oxygen, opt: v },
            })
          }
          onChangeMin={(v) =>
            setConsumption({
              ...consumption,
              oxygen: { ...consumption.oxygen, min: v },
            })
          }
          unit="Litrów"
        />
        <DoubleConfigRow
          label="Woda"
          valueOpt={consumption.water.opt}
          valueMin={consumption.water.min}
          onChangeOpt={(v) =>
            setConsumption({
              ...consumption,
              water: { ...consumption.water, opt: v },
            })
          }
          onChangeMin={(v) =>
            setConsumption({
              ...consumption,
              water: { ...consumption.water, min: v },
            })
          }
          unit="Litrów"
        />
      </div>

      <div
        className={`mt-auto flex w-full px-2 pt-6 ${standaloneMode ? "justify-between" : "justify-end"}`}
      >
        {standaloneMode ? (
          <>
            <button
              onClick={onCancel}
              className="text-red-500 hover:text-red-400 p-2"
            >
              <X size={32} strokeWidth={3} />
            </button>
            <button
              onClick={handleSave}
              className="text-green-500 hover:text-green-400 p-2"
            >
              <Check size={32} strokeWidth={3} />
            </button>
          </>
        ) : (
          <button
            onClick={handleSave}
            className="bg-mars-orange text-mars-background p-2.5 rounded-xl"
          >
            <ChevronRight size={24} strokeWidth={3} />
          </button>
        )}
      </div>
    </div>
  );
};
