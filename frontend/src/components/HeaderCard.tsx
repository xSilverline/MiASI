import React from "react";

interface HeaderCardProps {
  icon: React.ElementType;
  title: string;
  value: string;
}

export const HeaderCard: React.FC<HeaderCardProps> = ({
  icon: Icon,
  title,
  value,
}) => {
  return (
    <div className="bg-mars-itemBackground p-6 rounded-3xl flex items-center gap-6 shadow-md w-full">
      <div className="bg-mars-itemBackground p-4 rounded-full text-mars-orange shrink-0">
        <Icon size={40} />
      </div>
      <div>
        <p className="text-sm text-slate-400 uppercase tracking-wider">
          {title}
        </p>
        <p className="text-3xl font-bold text-mars-orange">{value}</p>
      </div>
    </div>
  );
};
