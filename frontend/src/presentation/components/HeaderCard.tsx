import React from "react";
import { Edit2 } from "lucide-react";

interface HeaderCardProps {
  icon: React.ElementType;
  title: string;
  value: string;
  onEdit?: () => void;
}

export const HeaderCard: React.FC<HeaderCardProps> = ({
  icon: Icon,
  title,
  value,
  onEdit,
}) => {
  return (
    <div className="bg-mars-itemBackground p-6 rounded-3xl flex items-center gap-6 shadow-md w-full relative group">
      <div className="bg-mars-itemBackground p-4 rounded-full text-mars-orange shrink-0">
        <Icon size={40} />
      </div>

      <div>
        <p className="text-sm text-slate-400 uppercase tracking-wider">
          {title}
        </p>
        <p className="text-3xl font-bold text-mars-orange">{value}</p>
      </div>

      {onEdit && (
        <button
          onClick={onEdit}
          className="absolute top-6 right-6 text-slate-500 hover:text-mars-orange transition-colors opacity-0 group-hover:opacity-100 cursor-pointer"
        >
          <Edit2 size={22} />
        </button>
      )}
    </div>
  );
};
