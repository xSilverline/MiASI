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

import type { ChartDataPoint } from "../../core/application/ports/IMissionRepository";

interface CustomXAxisTickProps {
  x?: number;
  y?: number;
  payload?: {
    value: number;
  };
  missionDuration: number;
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

const SAFE_THRESHOLD_RATIO = 0.05;
const NEGATIVE_SPACE_RATIO = 0.08;
const MAX_Y_REFERENCE_LINES = 32;

const getNiceStep = (range: number, targetTickCount = 8) => {
  if (!Number.isFinite(range) || range <= 0) return 1;

  const roughStep = range / targetTickCount;
  const magnitude = Math.pow(10, Math.floor(Math.log10(roughStep)));
  const normalized = roughStep / magnitude;

  if (normalized <= 1) return magnitude;
  if (normalized <= 2) return 2 * magnitude;
  if (normalized <= 5) return 5 * magnitude;

  return 10 * magnitude;
};

const formatValue = (value: number) => {
  if (!Number.isFinite(value)) return "0";

  return value.toLocaleString("pl-PL", {
    maximumFractionDigits: Math.abs(value) >= 100 ? 0 : 2,
  });
};

const getDynamicXTicks = (maxSol: number, missionDuration: number) => {
  const safeMaxSol = Math.max(1, Math.ceil(maxSol));
  const step = getNiceStep(safeMaxSol, 8);
  const ticks: number[] = [];

  for (let sol = 0; sol <= safeMaxSol; sol += step) {
    ticks.push(sol);
  }

  ticks.push(missionDuration, safeMaxSol);

  return [...new Set(ticks)]
    .filter((tick) => Number.isFinite(tick) && tick >= 0 && tick <= safeMaxSol)
    .sort((a, b) => a - b);
};

const getAbsoluteYAxis = (data: ChartDataPoint[]) => {
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
      minorTicks: [12.5, 37.5, 62.5, 87.5],
      safeThresholdValue: 5,
    };
  }

  const minValue = Math.min(...finiteValues);
  const maxValue = Math.max(...finiteValues);
  const positiveMax = Math.max(maxValue, 1);

  /**
   * Negative values are useful as a warning, but a small negative dip should not
   * consume a large part of the plot. We keep only a small negative band unless
   * the API data genuinely goes further below zero.
   */
  const negativeFloor = -positiveMax * NEGATIVE_SPACE_RATIO;
  const lowerRaw = minValue < 0 ? Math.max(minValue, negativeFloor) : 0;
  const upperRaw = positiveMax;
  const upperPadding = Math.max(upperRaw - lowerRaw, 1) * 0.08;

  const preliminaryMin = lowerRaw;
  const preliminaryMax = upperRaw + upperPadding;
  const step = getNiceStep(preliminaryMax - preliminaryMin, 6);

  const domainMin =
    preliminaryMin < 0 ? Math.floor(preliminaryMin / step) * step : 0;
  const domainMax = Math.ceil(preliminaryMax / step) * step;

  const majorTicks: number[] = [];
  for (let tick = domainMin; tick <= domainMax + step * 0.001; tick += step) {
    majorTicks.push(Number(tick.toFixed(6)));
  }

  majorTicks.push(0, domainMax);

  const uniqueMajorTicks = [...new Set(majorTicks)]
    .filter((tick) => tick >= domainMin && tick <= domainMax)
    .sort((a, b) => a - b);

  const minorTicks: number[] = [];
  const minorStep = step / 2;
  const estimatedMinorCount = Math.ceil((domainMax - domainMin) / minorStep);

  if (estimatedMinorCount <= MAX_Y_REFERENCE_LINES) {
    for (
      let tick = domainMin;
      tick <= domainMax + minorStep * 0.001;
      tick += minorStep
    ) {
      const rounded = Number(tick.toFixed(6));
      if (!uniqueMajorTicks.includes(rounded)) {
        minorTicks.push(rounded);
      }
    }
  }

  return {
    domain: [domainMin, domainMax] as [number, number],
    majorTicks: uniqueMajorTicks,
    minorTicks,
    safeThresholdValue: domainMax * SAFE_THRESHOLD_RATIO,
  };
};

const CustomXAxisTick: React.FC<CustomXAxisTickProps> = ({
  x = 0,
  y = 0,
  payload,
  missionDuration,
}) => {
  if (!payload) return null;

  const isMissionEnd = payload.value === missionDuration;

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
                {formatValue(Number(item.value))}
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

  const yAxis = getAbsoluteYAxis(data);
  const maxDataSol = Math.max(...data.map((point) => point.sol));
  const maxSol = Math.ceil(Math.max(missionDuration, maxDataSol) * 1.03);
  const xMajorTicks = getDynamicXTicks(maxSol, missionDuration);

  return (
    <div className="bg-mars-itemBackground p-6 rounded-xl shadow-md w-full flex flex-col flex-1 min-h-75">
      <div className="flex justify-between items-center mb-6 shrink-0">
        <h3 className="text-base font-semibold text-mars-orange uppercase tracking-wider">
          STAN ZASOBÓW
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
            <div className="w-3 h-0.5 bg-[#eab308]"></div>Energia
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
              tick={<CustomXAxisTick missionDuration={missionDuration} />}
            />
            <YAxis
              stroke="#94a3b8"
              tick={{ fontSize: 11 }}
              axisLine={false}
              tickLine={false}
              domain={yAxis.domain}
              ticks={yAxis.majorTicks}
              tickFormatter={(value) => formatValue(Number(value))}
              width={80}
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

            {/*<ReferenceLine*/}
            {/*  y={yAxis.safeThresholdValue}*/}
            {/*  stroke="#ef4444"*/}
            {/*  strokeDasharray="4 4"*/}
            {/*  opacity={0.6}*/}
            {/*  label={{*/}
            {/*    value: "SAFE: 5%",*/}
            {/*    position: "insideTopLeft",*/}
            {/*    fill: "#ef4444",*/}
            {/*    fontSize: 10,*/}
            {/*  }}*/}
            {/*/>*/}

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
