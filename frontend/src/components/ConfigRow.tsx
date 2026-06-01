import React from "react";
import { ChevronUp, ChevronDown } from "lucide-react";

interface ConfigRowProps {
  label: React.ReactNode;
  value: string;
  onChange: (value: string) => void;
  unit: string;
}

export const ConfigRow: React.FC<ConfigRowProps> = ({
  label,
  value,
  onChange,
  unit,
}) => {
  const handleIncrement = () => {
    const num = parseFloat(value) || 0;
    onChange((num + 0.1).toFixed(1));
  };

  const handleDecrement = () => {
    const num = parseFloat(value) || 0;
    onChange(Math.max(0, num - 0.1).toFixed(1));
  };

  return (
    <div className="grid grid-cols-[1fr_120px_80px] md:grid-cols-[1fr_160px_100px] items-center gap-4 md:gap-8 w-full">
      <div className="text-[10px] md:text-xs tracking-widest uppercase text-center leading-relaxed text-slate-200">
        {label}
      </div>

      <div className="relative w-full">
        <input
          type="number"
          value={value}
          onChange={(e) => onChange(e.target.value)}
          className="w-full bg-mars-line text-white pl-4 pr-8 py-3 rounded-xl text-center text-sm tracking-wide focus:outline-none focus:ring-1 focus:ring-mars-orange/40 transition-all font-medium shadow-inner"
        />

        <div className="absolute right-3 top-0 bottom-0 flex flex-col justify-center gap-1.5">
          <button
            type="button"
            onClick={handleIncrement}
            className="text-mars-orange hover:text-white transition-colors active:scale-90 cursor-pointer flex items-center justify-center"
          >
            <ChevronUp size={14} strokeWidth={4} />
          </button>
          <button
            type="button"
            onClick={handleDecrement}
            className="text-mars-orange hover:text-white transition-colors active:scale-90 cursor-pointer flex items-center justify-center"
          >
            <ChevronDown size={14} strokeWidth={4} />
          </button>
        </div>
      </div>

      <div className="text-[10px] md:text-xs tracking-widest text-slate-300">
        {unit}
      </div>
    </div>
  );
};
