import React, { useState } from "react";
import { Check, ChevronRight, X } from "lucide-react";
import { ConfigRow } from "../../components/ConfigRow";

interface ResourceConfigStepProps {
  onNext: () => void;
  standaloneMode?: boolean;
  onCancel?: () => void;
}

export const ResourceConfigStep: React.FC<ResourceConfigStepProps> = ({
  onNext,
  standaloneMode = false,
  onCancel,
}) => {
  const [maleFood, setMaleFood] = useState("");
  const [femaleFood, setFemaleFood] = useState("");
  const [oxygen, setOxygen] = useState("");
  const [water, setWater] = useState("");

  return (
    <div className="bg-mars-itemBackground py-12 px-6 rounded-4xl shadow-xl w-full flex flex-col h-160 relative">
      <h2 className="text-center font-bold tracking-widest text-sm uppercase mb-12 text-mars-orange">
        Załoga
      </h2>

      <div className="flex flex-col gap-12 w-full mt-auto">
        <ConfigRow
          label={
            <>
              Zużycie Racji
              <br />
              Żywnościowych
              <br />
              (Mężczyzna)
            </>
          }
          value={maleFood}
          onChange={setMaleFood}
          unit="Porcji"
        />
        <ConfigRow
          label={
            <>
              Zużycie Racji
              <br />
              Żywnościowych
              <br />
              (Kobieta)
            </>
          }
          value={femaleFood}
          onChange={setFemaleFood}
          unit="Porcji"
        />
        <ConfigRow
          label="Tlen / Sol"
          value={oxygen}
          onChange={setOxygen}
          unit="Litrów"
        />
        <ConfigRow
          label="Woda / Sol"
          value={water}
          onChange={setWater}
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
              onClick={onNext}
              className="text-green-500 hover:text-green-400 p-2 transition-all active:scale-95 cursor-pointer"
            >
              <Check size={32} strokeWidth={3} />
            </button>
          </>
        ) : (
          <button
            onClick={onNext}
            className="bg-mars-orange hover:bg-mars-orange/90 text-mars-background p-2.5 rounded-xl transition-all shadow-md active:scale-95 cursor-pointer"
          >
            <ChevronRight size={24} strokeWidth={3} />
          </button>
        )}
      </div>
    </div>
  );
};
