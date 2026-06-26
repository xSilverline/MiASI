import { useState, useEffect, useCallback } from "react";
import type { MissionDashboardConfig } from "../../core/domain/entities/MissionConfig";
import { missionRepository } from "../../infrastructure/dependencyInjection/container";
import type { ChartDataPoint } from "../../core/application/ports/IMissionRepository";

export const useMissionData = () => {
  const [config, setConfig] = useState<MissionDashboardConfig | null>(null);
  const [isLoading, setIsLoading] = useState<boolean>(true);

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
      return data;
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

    try {
      await missionRepository.saveConfig(updatedConfig);

      const freshConfig = await missionRepository.getConfig();
      if (freshConfig) {
        setConfig(freshConfig);
      }
    } catch (error) {
      console.error("Błąd zapisu", error);
    }
  };

  const optimize = async () => {
    if (!config) return;
    setIsOptimizing(true);
    try {
      // 1. Pobieramy zoptymalizowany plan z API
      const { payloadSessionId: newPayloadId, updatedConfig } =
        await missionRepository.optimize();
      setPayloadSessionId(newPayloadId);

      // 2. Bezpieczne łączenie (Merge) - zachowujemy wszystko to, czego
      // optymalizacja mogła nie zwrócić (np. eventsList)
      const mergedConfig = {
        ...config,
        ...updatedConfig,
        // Zapewniamy, że tablice są zawsze tablicami, nawet jeśli API zwróci undefined
        modulesList: updatedConfig.modulesList || config.modulesList,
        startingResources:
          updatedConfig.startingResources || config.startingResources,
      };

      // 3. Zapisujemy zmergowany stan lokalnie
      setConfig(mergedConfig);

      // 4. TWARDY ZAPIS do bazy (niezbędne, aby kolejne wywołania API wiedziały o zmianach)
      await missionRepository.saveConfig(mergedConfig);

      // 5. Ostateczna synchronizacja (Pobranie "Source of Truth" z bazy)
      const freshConfig = await missionRepository.getConfig();
      if (freshConfig) {
        setConfig(freshConfig);
      }

      // 6. Rekalkulacja wykresów na już zapisanych danych
      setIsRecalculating(true);
      const { nominalSessionId: newNominalId, chartData: newChartData } =
        await missionRepository.recalculate(newPayloadId);

      setNominalSessionId(newNominalId);
      setChartData(newChartData);
      setIsDataModified(false);
    } catch (error) {
      console.error("Błąd podczas optymalizacji i synchronizacji:", error);
    } finally {
      setIsOptimizing(false);
      setIsRecalculating(false);
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
    nominalSessionId,
    chartData,
    updateConfig,
    optimize,
    recalculate,
    loadConfig,
  };
};
