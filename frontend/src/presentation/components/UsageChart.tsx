import React from "react";
import {
  ResponsiveContainer,
  LineChart,
  Line,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ReferenceLine,
} from "recharts";
import {
  MISSION_DURATION,
  TOTAL_SIMULATION_DAYS,
  MIN_RESERVE,
  DELIVERY_SOL,
} from "../../infrastructure/mock-data/config.ts";
import type { ChartDataPoint } from "../../core/application/ports/IMissionRepository";
interface CustomXAxisTickProps {
  x?: number;
  y?: number;
  payload?: {
    value: number;
  };
}
interface UsageChartProps {
  data: ChartDataPoint[];
}

interface CustomTooltipProps {
  active?: boolean;
  payload?: Array<{
    name: string;
    value: number;
    stroke: string;
  }>;
  label?: string | number;
}

// const generateMockData = () => {
//   const data = [];
//   let woda = 100.0;
//   let tlen = 100.0;
//   let zywnosc = 100.0;
//
//   for (let sol = 0; sol <= TOTAL_SIMULATION_DAYS; sol++) {
//     if (sol > 0) {
//       woda -= 0.12;
//       tlen -= 0.13;
//       if (sol === DELIVERY_SOL) zywnosc += 25;
//       zywnosc -= 0.14;
//     }
//     data.push({
//       sol,
//       woda: Math.max(0, Number(woda.toFixed(2))),
//       tlen: Math.max(0, Number(tlen.toFixed(2))),
//       zywnosc: Math.max(0, Number(zywnosc.toFixed(2))),
//       energia: 8.0,
//     });
//   }
//   return data;
// };

// const chartData = generateMockData();

const xMajorTicks: number[] = [];
for (let i = 0; i <= TOTAL_SIMULATION_DAYS; i += 25) {
  xMajorTicks.push(i);
}

const yMajorTicks = [0, 25, 50, 75, 100];
const yMinorTicks = [10, 20, 30, 40, 60, 70, 80, 90];

const CustomXAxisTick: React.FC<CustomXAxisTickProps> = ({
  x = 0,
  y = 0,
  payload,
}) => {
  if (!payload) return null;

  const isMissionEnd = payload.value === MISSION_DURATION;
  return (
    <g transform={`translate(${x},${y})`}>
      <text
        x={0}
        y={0}
        dy={12}
        textAnchor="middle"
        fill={isMissionEnd ? "#ff0000" : "#94a3b8"}
        fontSize={10}
        fontWeight={isMissionEnd ? "bold" : "normal"}
      >
        {payload.value}
      </text>
    </g>
  );
};

const CustomTooltip: React.FC<CustomTooltipProps> = ({
  active,
  payload,
  label,
}) => {
  if (active && payload && payload.length) {
    return (
      <div className="bg-mars-itemBackground p-4 rounded-xl shadow-lg flex flex-col gap-2 min-w-37.5">
        <p className="text-mars-orange font-bold text-xs">SOL {label}</p>
        <div className="space-y-1.5">
          {payload.map((item, index) => (
            <p
              key={index}
              className="text-[11px] flex justify-between gap-4"
              style={{ color: item.stroke }}
            >
              <span className="uppercase tracking-wide">{item.name}:</span>
              <span className="text-white font-bold">
                {Number(item.value).toFixed(2)}%
              </span>
            </p>
          ))}
        </div>
      </div>
    );
  }
  return null;
};

export const UsageChart: React.FC<UsageChartProps> = ({ data }) => {
  if (!data || data.length === 0) {
    return (
      <div className="flex h-full items-center justify-center text-slate-500 uppercase text-xs tracking-widest">
        Brak danych - przeprowadź rekalkulację
      </div>
    );
  }
  return (
    <div className="bg-mars-itemBackground p-6 rounded-xl shadow-md w-full flex flex-col flex-1 min-h-75">
      <div className="flex justify-between items-center mb-6 shrink-0">
        <h3 className="text-base font-semibold text-mars-orange uppercase tracking-wider">
          WYKRES ZUŻYCIA
        </h3>
        <div className="flex gap-4 text-xs text-slate-400 uppercase">
          <span className="flex items-center gap-1.5">
            <div className="w-3 h-0.5 bg-[#22d3ee]"></div>Woda
          </span>
          <span className="flex items-center gap-1.5">
            <div className="w-3 h-0.5 bg-[#a855f7]"></div>Tlen
          </span>
          <span className="flex items-center gap-1.5">
            <div className="w-3 h-0.5 bg-[#16a34a]"></div>Żywność
          </span>
          <span className="flex items-center gap-1.5">
            <div className="w-3 h-0.5 bg-[#eab308]"></div>Bilans Energii
          </span>
        </div>
      </div>

      <div className="flex-1 h-0 w-full">
        <ResponsiveContainer width="100%" height="100%">
          <LineChart
            data={data}
            margin={{ top: 10, right: 20, left: -20, bottom: 5 }}
          >
            <CartesianGrid stroke="none" />
            <XAxis
              dataKey="sol"
              type="number"
              domain={[0, TOTAL_SIMULATION_DAYS]}
              ticks={xMajorTicks}
              stroke="#94a3b8"
              axisLine={false}
              tickLine={false}
              tick={<CustomXAxisTick />}
            />
            <YAxis
              stroke="#94a3b8"
              tick={{ fontSize: 11 }}
              axisLine={false}
              tickLine={false}
              domain={[0, 100]}
              ticks={yMajorTicks}
            />

            {xMajorTicks.map((sol) => (
              <ReferenceLine
                key={`x-major-${sol}`}
                x={sol}
                stroke="var(--color-mars-line)"
                opacity={1.0}
              />
            ))}
            {yMinorTicks.map((yVal) => (
              <ReferenceLine
                key={`y-minor-${yVal}`}
                y={yVal}
                stroke="var(--color-mars-line)"
                opacity={0.5}
              />
            ))}
            {yMajorTicks.map((yVal) => (
              <ReferenceLine
                key={`y-major-${yVal}`}
                y={yVal}
                stroke="var(--color-mars-line)"
                opacity={1}
              />
            ))}

            <ReferenceLine
              y={MIN_RESERVE}
              stroke="#ef4444"
              strokeDasharray="4 4"
              opacity={0.6}
              label={{
                value: `MIN: ${MIN_RESERVE}%`,
                position: "insideTopLeft",
                fill: "#ef4444",
                fontSize: 10,
              }}
            />
            <ReferenceLine
              x={DELIVERY_SOL}
              stroke="#16a34a"
              opacity={0.4}
              strokeWidth={3}
            />
            <ReferenceLine
              x={MISSION_DURATION}
              stroke="#ff0000"
              opacity={0.5}
              strokeWidth={3}
            />

            <Line
              type="monotone"
              dataKey="woda"
              stroke="#22d3ee"
              strokeWidth={2}
              dot={false}
              name="Woda"
            />
            <Line
              type="monotone"
              dataKey="tlen"
              stroke="#a855f7"
              strokeWidth={2}
              dot={false}
              name="Tlen"
            />
            <Line
              type="monotone"
              dataKey="zywnosc"
              stroke="#16a34a"
              strokeWidth={2}
              dot={false}
              name="Żywność"
            />
            <Line
              type="monotone"
              dataKey="energia"
              stroke="#eab308"
              strokeWidth={2}
              dot={false}
              name="Energia"
            />

            <Tooltip
              content={<CustomTooltip />}
              cursor={{ stroke: "var(--color-mars-line)", strokeWidth: 1 }}
            />
          </LineChart>
        </ResponsiveContainer>
      </div>
    </div>
  );
};
