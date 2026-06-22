import React from "react";
import { Edit2 } from "lucide-react";

export interface DetailItem {
  label: string;
  value: string;
  color?: string;
  valueSuffix?: string;
}

interface DetailCardProps {
  title: string;
  items: DetailItem[];
  onEdit?: () => void;
}

export const DetailCard: React.FC<DetailCardProps> = ({
  title,
  items,
  onEdit,
}) => {
  return (
    <div className="bg-mars-itemBackground p-8 rounded-3xl shadow-md flex flex-col relative group">
      <div className="flex justify-between items-center mb-6">
        <h3 className="text-mars-orange text-xs tracking-widest font-bold uppercase">
          {title}
        </h3>
        {onEdit && (
          <button
            onClick={onEdit}
            className="text-slate-500 hover:text-mars-orange transition-colors opacity-0 group-hover:opacity-100 cursor-pointer"
          >
            <Edit2 size={18} />
          </button>
        )}
      </div>

      <div className="flex flex-col gap-5">
        {items.map((item, idx) => (
          <div
            key={idx}
            className="flex justify-between items-center border-b border-mars-line/50 pb-3 last:border-0 last:pb-0"
          >
            <span className="text-xs text-slate-400 tracking-widest">
              {item.label}
            </span>
            <span
              className={`text-sm font-bold tracking-wider ${item.color || "text-white"}`}
            >
              {item.value}{" "}
              {item.valueSuffix && (
                <span className="text-[10px] text-slate-500">
                  {item.valueSuffix}
                </span>
              )}
            </span>
          </div>
        ))}
      </div>
    </div>
  );
};
