import React, { useMemo, useState } from "react";
import {
  AlertTriangle,
  Activity,
  CheckCircle2,
  Loader2,
  Play,
  Radiation,
  Rocket,
  ShieldAlert,
} from "lucide-react";
import {
  Line,
  LineChart,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
  CartesianGrid,
  ReferenceLine,
} from "recharts";
import type { ScenarioDifficulty } from "../../../core/application/ports/ISimulationRepository";
import type { EventData } from "../../../core/domain/entities/event";
import { useSimulationData } from "../../hooks/useSimulationData";

interface SimulationViewProps {
  mode: "automatic" | "manual";
  missionDuration: number;
  nominalSessionId: string | null;
  availableEvents: EventData[];
}

const difficultyOptions: { value: ScenarioDifficulty; label: string }[] = [
  { value: "LEVEL_I", label: "Poziom I" },
  { value: "LEVEL_II", label: "Poziom II" },
  { value: "LEVEL_III", label: "Poziom III" },
  { value: "LEVEL_IV", label: "Poziom IV" },
  { value: "LEVEL_V", label: "Poziom V" },
];

const statusLabels = {
  SUCCESS: "SUKCES",
  FAILURE: "PORAŻKA",
  EVACUATION: "EWAKUACJA",
};

export const SimulationView: React.FC<SimulationViewProps> = ({
  mode,
  missionDuration,
  nominalSessionId,
  availableEvents,
}) => {
  const [difficulty, setDifficulty] = useState<ScenarioDifficulty>("LEVEL_III");
  const {
    isLoading,
    error,
    scheduleId,
    draftId,
    proposedEventsCount,
    result,
    runAutomaticSimulation,
    runManualSimulation,
  } = useSimulationData();

  const isAutomatic = mode === "automatic";
  const canRun = Boolean(nominalSessionId) && !isLoading;

  const chartData = useMemo(() => {
    const ideal = result?.idealTimeline || [];
    const real = result?.realTimeline || [];
    const realBySol = new Map(real.map((point) => [point.sol, point]));

    return ideal.map((point) => {
      const realPoint = realBySol.get(point.sol);
      return {
        sol: point.sol,
        idealWater: point.waterStore,
        realWater: realPoint?.waterStore ?? null,
        idealOxygen: point.oxygenStore,
        realOxygen: realPoint?.oxygenStore ?? null,
        idealFood: point.foodStore,
        realFood: realPoint?.foodStore ?? null,
        idealEnergy: point.energyStore,
        realEnergy: realPoint?.energyStore ?? null,
      };
    });
  }, [result]);

  const handleRun = () => {
    if (!nominalSessionId) return;

    if (isAutomatic) {
      void runAutomaticSimulation({
        nominalSessionId,
        missionDuration,
        difficulty,
      });
      return;
    }

    void runManualSimulation({
      nominalSessionId,
      missionDuration,
    });
  };

  const outcome = result?.realOutcome;
  const outcomeLabel = outcome?.status ? statusLabels[outcome.status] : "BRAK DANYCH";

  return (
    <div className="flex flex-col h-full gap-8 overflow-hidden">
      <div className="grid grid-cols-3 gap-8 shrink-0">
        <div className="bg-mars-itemBackground p-6 rounded-3xl shadow-md border border-mars-orange/10 flex items-center gap-5">
          <div className="bg-mars-background p-4 rounded-full text-mars-orange shrink-0">
            {isAutomatic ? <Rocket size={34} /> : <Activity size={34} />}
          </div>
          <div>
            <p className="text-xs text-slate-400 uppercase tracking-widest">Tryb</p>
            <p className="text-2xl font-bold text-mars-orange uppercase">
              {isAutomatic ? "Automatyczny" : "Ręczny"}
            </p>
          </div>
        </div>

        <div className="bg-mars-itemBackground p-6 rounded-3xl shadow-md border border-mars-orange/10 flex items-center gap-5">
          <div className="bg-mars-background p-4 rounded-full text-mars-orange shrink-0">
            <ShieldAlert size={34} />
          </div>
          <div>
            <p className="text-xs text-slate-400 uppercase tracking-widest">Wynik realny</p>
            <p className="text-2xl font-bold text-white uppercase">{outcomeLabel}</p>
          </div>
        </div>

        <div className="bg-mars-itemBackground p-6 rounded-3xl shadow-md border border-mars-orange/10 flex items-center gap-5">
          <div className="bg-mars-background p-4 rounded-full text-mars-orange shrink-0">
            <Radiation size={34} />
          </div>
          <div>
            <p className="text-xs text-slate-400 uppercase tracking-widest">Zagrożenia</p>
            <p className="text-2xl font-bold text-white uppercase">
              {result?.appliedThreatsCount ?? proposedEventsCount}
            </p>
          </div>
        </div>
      </div>

      <div className="grid grid-cols-[360px_1fr] gap-8 min-h-0 flex-1">
        <aside className="bg-mars-itemBackground rounded-3xl p-8 border border-mars-orange/10 shadow-lg flex flex-col min-h-0">
          <h2 className="text-mars-orange text-sm font-bold tracking-widest uppercase mb-8 text-center">
            Parametry symulacji
          </h2>

          <div className="flex flex-col gap-5 text-xs tracking-widest uppercase">
            <div className="bg-mars-background rounded-xl p-4 border border-mars-line/50">
              <p className="text-slate-500 mb-2">Czas misji</p>
              <p className="text-white font-bold text-lg">{missionDuration} SOL</p>
            </div>

            <div className="bg-mars-background rounded-xl p-4 border border-mars-line/50">
              <p className="text-slate-500 mb-2">Sesja nominalna</p>
              <p className="text-white font-bold text-sm break-all">
                {nominalSessionId || "Brak - wykonaj rekalkulację"}
              </p>
            </div>

            {isAutomatic && (
              <div className="bg-mars-background rounded-xl p-4 border border-mars-line/50">
                <label className="block text-slate-500 mb-3">Trudność scenariusza</label>
                <select
                  value={difficulty}
                  onChange={(e) => setDifficulty(e.target.value as ScenarioDifficulty)}
                  className="w-full bg-mars-line text-white px-4 py-3 rounded-xl text-center text-xs tracking-widest focus:outline-none focus:ring-1 focus:ring-mars-orange/40 appearance-none cursor-pointer"
                >
                  {difficultyOptions.map((option) => (
                    <option key={option.value} value={option.value}>
                      {option.label}
                    </option>
                  ))}
                </select>
              </div>
            )}

            {!isAutomatic && (
              <div className="bg-mars-background rounded-xl p-4 border border-mars-line/50">
                <p className="text-slate-500 mb-2">Katalog zdarzeń</p>
                <p className="text-white font-bold text-lg">{availableEvents.length}</p>
              </div>
            )}

            {draftId && (
              <div className="bg-mars-background rounded-xl p-4 border border-mars-line/50">
                <p className="text-slate-500 mb-2">Draft scenariusza</p>
                <p className="text-white font-bold text-sm break-all">{draftId}</p>
              </div>
            )}

            {scheduleId && (
              <div className="bg-mars-background rounded-xl p-4 border border-mars-line/50">
                <p className="text-slate-500 mb-2">Schedule ID</p>
                <p className="text-white font-bold text-sm break-all">{scheduleId}</p>
              </div>
            )}
          </div>

          {error && (
            <div className="mt-6 bg-red-500/10 border border-red-500/30 text-red-400 rounded-xl p-4 text-xs leading-relaxed">
              {error}
            </div>
          )}

          {!nominalSessionId && (
            <div className="mt-6 flex gap-3 bg-mars-orange/10 border border-mars-orange/30 text-mars-orange rounded-xl p-4 text-xs leading-relaxed">
              <AlertTriangle size={18} className="shrink-0" />
              <span>Najpierw uruchom auto-optymalizację i rekalkulację wykresów w podsumowaniu.</span>
            </div>
          )}

          <button
            onClick={handleRun}
            disabled={!canRun}
            className="mt-auto flex items-center justify-center gap-3 py-4 px-6 rounded-xl text-xs tracking-widest font-bold uppercase bg-mars-orange/10 text-mars-orange border border-mars-orange/30 hover:bg-mars-orange/20 hover:border-mars-orange transition-all disabled:opacity-50 disabled:cursor-not-allowed"
          >
            {isLoading ? <Loader2 size={18} className="animate-spin" /> : <Play size={18} />}
            {isLoading ? "Trwa symulacja" : "Uruchom symulację"}
          </button>
        </aside>

        <section className="bg-mars-itemBackground rounded-3xl p-8 border border-mars-orange/10 shadow-lg flex flex-col min-h-0">
          <div className="flex justify-between items-center mb-6 shrink-0">
            <h2 className="text-xl font-bold tracking-widest text-white uppercase">
              Porównanie wariantu idealnego i realnego
            </h2>
            {result && (
              <div className="flex items-center gap-2 text-green-500 text-xs tracking-widest uppercase font-bold">
                <CheckCircle2 size={18} /> Sesja {result.sessionId}
              </div>
            )}
          </div>

          {!result ? (
            <div className="flex-1 flex flex-col items-center justify-center text-center bg-mars-background rounded-2xl border border-mars-line/50">
              <Activity size={52} className="text-mars-orange mb-5" strokeWidth={1.5} />
              <h3 className="text-white font-bold tracking-widest uppercase mb-3">
                Brak wyniku symulacji
              </h3>
              <p className="text-slate-400 text-sm max-w-lg leading-relaxed">
                Uruchom symulację, aby porównać przebieg idealny z realnym scenariuszem awarii, dostaw i zmian stanu modułów.
              </p>
            </div>
          ) : (
            <div className="flex-1 h-0 min-h-0 bg-mars-background rounded-2xl border border-mars-line/50 p-5">
              <ResponsiveContainer width="100%" height="100%">
                <LineChart data={chartData} margin={{ top: 10, right: 20, left: -20, bottom: 5 }}>
                  <CartesianGrid stroke="var(--color-mars-line)" opacity={0.35} />
                  <XAxis dataKey="sol" stroke="#94a3b8" tick={{ fontSize: 11 }} />
                  <YAxis stroke="#94a3b8" tick={{ fontSize: 11 }} />
                  <ReferenceLine x={missionDuration} stroke="#ef4444" opacity={0.7} strokeWidth={2} />
                  <Tooltip
                    contentStyle={{
                      background: "var(--color-mars-itemBackground)",
                      border: "1px solid var(--color-mars-line)",
                      borderRadius: "12px",
                      color: "#fff",
                    }}
                  />

                  <Line type="monotone" dataKey="idealWater" name="Woda idealna" stroke="#22d3ee" strokeWidth={1.5} dot={false} strokeDasharray="4 4" />
                  <Line type="monotone" dataKey="realWater" name="Woda realna" stroke="#22d3ee" strokeWidth={2.5} dot={false} />
                  <Line type="monotone" dataKey="idealOxygen" name="Tlen idealny" stroke="#a855f7" strokeWidth={1.5} dot={false} strokeDasharray="4 4" />
                  <Line type="monotone" dataKey="realOxygen" name="Tlen realny" stroke="#a855f7" strokeWidth={2.5} dot={false} />
                  <Line type="monotone" dataKey="idealFood" name="Żywność idealna" stroke="#16a34a" strokeWidth={1.5} dot={false} strokeDasharray="4 4" />
                  <Line type="monotone" dataKey="realFood" name="Żywność realna" stroke="#16a34a" strokeWidth={2.5} dot={false} />
                  <Line type="monotone" dataKey="idealEnergy" name="Energia idealna" stroke="#eab308" strokeWidth={1.5} dot={false} strokeDasharray="4 4" />
                  <Line type="monotone" dataKey="realEnergy" name="Energia realna" stroke="#eab308" strokeWidth={2.5} dot={false} />
                </LineChart>
              </ResponsiveContainer>
            </div>
          )}
        </section>
      </div>
    </div>
  );
};
