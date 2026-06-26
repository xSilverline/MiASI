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
  missionDuration: number;
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

const getNiceStep = (range: number) => {
  if (range <= 0) return 1;

  const roughStep = range / 5;
  const magnitude = Math.pow(10, Math.floor(Math.log10(roughStep)));
  const normalized = roughStep / magnitude;

  if (normalized <= 1) return magnitude;
  if (normalized <= 2) return 2 * magnitude;
  if (normalized <= 5) return 5 * magnitude;

  return 10 * magnitude;
};

const getDynamicYAxis = (data: ChartDataPoint[]) => {
  const values = data.flatMap((point) => [
    point.waterStore,
    point.oxygenStore,
    point.foodStore,
    point.energyStore,
  ]);

  const finiteValues = values.filter(Number.isFinite);

  if (finiteValues.length === 0) {
    return {
      domain: [0, 100] as [number, number],
      majorTicks: [0, 25, 50, 75, 100],
      minorTicks: [10, 20, 30, 40, 60, 70, 80, 90],
    };
  }

  const minValue = Math.min(...finiteValues);
  const maxValue = Math.max(...finiteValues);

  const lowerRaw = Math.min(0, minValue);
  const upperRaw = maxValue;

  const range = upperRaw - lowerRaw || Math.max(Math.abs(upperRaw), 1);
  const padding = range * 0.08;

  const paddedMin = lowerRaw - padding;
  const paddedMax = upperRaw + padding;

  const step = getNiceStep(paddedMax - paddedMin);

  const domainMin = Math.floor(paddedMin / step) * step;
  const domainMax = Math.ceil(paddedMax / step) * step;

  const majorTicks: number[] = [];
  for (let tick = domainMin; tick <= domainMax + step * 0.001; tick += step) {
    majorTicks.push(Number(tick.toFixed(6)));
  }

  const minorStep = step / 2;
  const minorTicks: number[] = [];

  for (let tick = domainMin + minorStep; tick < domainMax; tick += minorStep) {
    const rounded = Number(tick.toFixed(6));
    if (!majorTicks.includes(rounded)) {
      minorTicks.push(rounded);
    }
  }

  return {
    domain: [domainMin, domainMax] as [number, number],
    majorTicks,
    minorTicks,
  };
};

const formatAxisValue = (value: number) => {
  if (Math.abs(value) >= 1000) {
    return value.toLocaleString("pl-PL", {
      maximumFractionDigits: 0,
    });
  }

  if (Math.abs(value) >= 10) {
    return value.toLocaleString("pl-PL", {
      maximumFractionDigits: 1,
    });
  }

  return value.toLocaleString("pl-PL", {
    maximumFractionDigits: 2,
  });
};

const xMajorTicks: number[] = [];
for (let i = 0; i <= TOTAL_SIMULATION_DAYS; i += 25) {
  xMajorTicks.push(i);
}

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
                {formatAxisValue(Number(item.value))}
              </span>
            </p>
          ))}
        </div>
      </div>
    );
  }
  return null;
};

export const UsageChart: React.FC<UsageChartProps> = ({
  data,
  missionDuration,
}) => {
  if (!data || data.length === 0) {
    return (
      <div className="flex h-full items-center justify-center text-slate-500 uppercase text-xs tracking-widest">
        Brak danych - przeprowadź rekalkulację
      </div>
    );
  }
  const yAxis = getDynamicYAxis(data);
  const maxSol = Math.ceil(missionDuration * 1.05);
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
              domain={[0, maxSol]}
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
              domain={yAxis.domain}
              ticks={yAxis.majorTicks}
              tickFormatter={(value) => formatAxisValue(Number(value))}
              width={70}
            />

            {xMajorTicks.map((sol) => (
              <ReferenceLine
                key={`x-major-${sol}`}
                x={sol}
                stroke="var(--color-mars-line)"
                opacity={1.0}
              />
            ))}
            {yAxis.minorTicks.map((yVal) => (
              <ReferenceLine
                key={`y-minor-${yVal}`}
                y={yVal}
                stroke="var(--color-mars-line)"
                opacity={0.5}
              />
            ))}

            {yAxis.majorTicks.map((yVal) => (
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
              x={missionDuration}
              stroke="#ff0000"
              opacity={0.5}
              strokeWidth={3}
            />

            <Line
              type="monotone"
              dataKey="waterStore"
              stroke="#22d3ee"
              strokeWidth={2}
              dot={false}
              name="Woda"
            />
            <Line
              type="monotone"
              dataKey="oxygenStore"
              stroke="#a855f7"
              strokeWidth={2}
              dot={false}
              name="Tlen"
            />
            <Line
              type="monotone"
              dataKey="foodStore"
              stroke="#16a34a"
              strokeWidth={2}
              dot={false}
              name="Żywność"
            />
            <Line
              type="monotone"
              dataKey="energyStore"
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
