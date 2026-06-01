import React from "react";

export interface DetailItem {
  label: string;
  value: string;
  color?: string;
  valueSuffix?: string;
}

interface DetailCardProps {
  title: string;
  items: DetailItem[];
}

export const DetailCard: React.FC<DetailCardProps> = ({ title, items }) => {
  return (
    <div className="bg-mars-itemBackground p-6 rounded-xl shadow-md h-full">
      <h3 className="text-base font-semibold text-mars-orange uppercase tracking-wider mb-5">
        {title}
      </h3>
      <ul className="space-y-3">
        {items.map((item, idx) => (
          <li key={idx} className="flex justify-between items-center text-sm">
            <span className="text-slate-300 uppercase tracking-wide pr-2">
              {item.label}
            </span>
            <span
              className={`font-medium whitespace-nowrap ${item.color || "text-white"}`}
            >
              {item.value}{" "}
              {item.valueSuffix && (
                <span className="text-xs ml-1">{item.valueSuffix}</span>
              )}
            </span>
          </li>
        ))}
      </ul>
    </div>
  );
};
