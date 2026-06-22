import React from "react";

interface DoubleConfigRowProps {
  label: React.ReactNode;
  valueOpt: string;
  valueMin: string;
  onChangeOpt: (v: string) => void;
  onChangeMin: (v: string) => void;
  unit: string;
}

export const DoubleConfigRow: React.FC<DoubleConfigRowProps> = ({
  label,
  valueOpt,
  valueMin,
  onChangeOpt,
  onChangeMin,
  unit,
}) => {
  const handleBlockInvalidFloats = (
    e: React.KeyboardEvent<HTMLInputElement>,
  ) => {
    if (["e", "E", "+", "-"].includes(e.key)) e.preventDefault();
  };

  return (
    <div className="grid grid-cols-[120px_1fr_1fr] md:grid-cols-[140px_1fr_1fr] items-end gap-4 md:gap-6">
      <label className="text-[10px] md:text-xs tracking-widest uppercase text-slate-200 text-right pb-3">
        {label}
      </label>

      <div className="flex flex-col relative w-full">
        <span className="text-[8px] md:text-[9px] text-slate-400 uppercase mb-2 text-center font-bold tracking-widest">
          Optymalne
        </span>
        <div className="relative">
          <input
            type="number"
            min="0"
            step="0.1"
            value={valueOpt}
            onKeyDown={handleBlockInvalidFloats}
            onChange={(e) => onChangeOpt(e.target.value)}
            className="w-full bg-mars-line text-white pl-2 pr-10 py-3 rounded-xl text-center text-[10px] md:text-sm font-medium focus:outline-none focus:ring-1 focus:ring-mars-orange/40 transition-all shadow-inner"
          />
          <span className="absolute right-3 top-1/2 -translate-y-1/2 text-[8px] md:text-[9px] text-slate-500 uppercase pointer-events-none">
            {unit}
          </span>
        </div>
      </div>

      <div className="flex flex-col relative w-full">
        <span className="text-[8px] md:text-[9px] text-slate-400 uppercase mb-2 text-center font-bold tracking-widest">
          Minimalne
        </span>
        <div className="relative">
          <input
            type="number"
            min="0"
            step="0.1"
            value={valueMin}
            onKeyDown={handleBlockInvalidFloats}
            onChange={(e) => onChangeMin(e.target.value)}
            className="w-full bg-mars-line text-white pl-2 pr-10 py-3 rounded-xl text-center text-[10px] md:text-sm font-medium focus:outline-none focus:ring-1 focus:ring-mars-orange/40 transition-all shadow-inner"
          />
          <span className="absolute right-3 top-1/2 -translate-y-1/2 text-[8px] md:text-[9px] text-slate-500 uppercase pointer-events-none">
            {unit}
          </span>
        </div>
      </div>
    </div>
  );
};
