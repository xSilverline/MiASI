import React, { useState } from "react";
import { ChevronRight } from "lucide-react";
import { ConfigRow } from "../../components/ConfigRow";

interface ResourceConfigStepProps {
  onNext: () => void;
}

export const ResourceConfigStep: React.FC<ResourceConfigStepProps> = ({
  onNext,
}) => {
  const [maleFood, setMaleFood] = useState("");
  const [femaleFood, setFemaleFood] = useState("");
  const [oxygen, setOxygen] = useState("");
  const [water, setWater] = useState("");

  return (
    <div className="bg-mars-itemBackground py-14 px-4 rounded-4xl shadow-xl w-full flex flex-col relative">
      <h2 className="text-center font-bold tracking-widest text-sm uppercase mb-12 text-mars-orange">
        Załoga
      </h2>

      <div className="flex flex-col gap-8 w-full">
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

      <div className="mt-14 flex justify-end w-full">
        <button
          onClick={onNext}
          className="bg-mars-orange hover:bg-mars-orange/90 text-mars-background p-2.5 rounded-xl transition-all shadow-md active:scale-95 cursor-pointer"
        >
          <ChevronRight size={24} strokeWidth={3} />
        </button>
      </div>
    </div>
  );
};
