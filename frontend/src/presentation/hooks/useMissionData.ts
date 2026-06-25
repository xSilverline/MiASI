import { useState, useEffect, useCallback } from "react";
import type { MissionDashboardConfig } from "../../core/domain/entities/MissionConfig";
import { missionRepository } from "../../infrastructure/dependencyInjection/container";
import type { ChartDataPoint } from "../../core/application/ports/IMissionRepository";

export const useMissionData = () => {
  const [config, setConfig] = useState<MissionDashboardConfig | null>(null);
  const [isLoading, setIsLoading] = useState<boolean>(true);

  // Zmienne stanowe do śledzenia statusu API i sesji
  const [isDataModified, setIsDataModified] = useState<boolean>(false);
  const [isOptimizing, setIsOptimizing] = useState<boolean>(false);
  const [isRecalculating, setIsRecalculating] = useState<boolean>(false);
  const [payloadSessionId, setPayloadSessionId] = useState<string | null>(null);

  const [chartData, setChartData] = useState<ChartDataPoint[]>([]);
  const [nominalSessionId, setNominalSessionId] = useState<string | null>(null);

  const loadConfig = useCallback(async () => {
    setIsLoading(true);
    try {
      const data = await missionRepository.getConfig();
      setConfig(data);
      return data; // Zwracamy pobrane dane (lub null), żeby App.tsx mogło podjąć decyzję o routingu
    } catch (error) {
      console.error("Błąd pobierania konfiguracji", error);
      return null;
    } finally {
      setIsLoading(false);
    }
  }, []);

  const updateConfig = async (newData: Partial<MissionDashboardConfig>) => {
    const updatedConfig = config
      ? { ...config, ...newData }
      : (newData as MissionDashboardConfig);

    setConfig(updatedConfig);
    setIsDataModified(true);
    setPayloadSessionId(null);

    try {
      await missionRepository.saveConfig(newData);
    } catch (error) {
      console.error("Błąd zapisu", error);
    }
  };

  const optimize = async () => {
    setIsOptimizing(true);
    try {
      const { payloadSessionId, updatedConfig } =
        await missionRepository.optimize();
      setPayloadSessionId(payloadSessionId);
      setConfig((prev) => (prev ? { ...prev, ...updatedConfig } : null));
    } catch (error) {
      console.error("Błąd optymalizacji", error);
    } finally {
      setIsOptimizing(false);
    }
  };

  const recalculate = async () => {
    if (!payloadSessionId) return;
    setIsRecalculating(true);
    try {
      const { nominalSessionId, chartData: newChartData } =
        await missionRepository.recalculate(payloadSessionId);

      setNominalSessionId(nominalSessionId);
      setChartData(newChartData);
      setIsDataModified(false);
    } catch (error) {
      console.error("Błąd rekalkulacji", error);
    } finally {
      setIsRecalculating(false);
    }
  };

  return {
    config,
    isLoading,
    isDataModified,
    setIsDataModified,
    isOptimizing,
    isRecalculating,
    payloadSessionId,
    chartData,
    updateConfig,
    optimize,
    recalculate,
    loadConfig,
  };
};
