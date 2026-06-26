import { useState, useEffect, useCallback } from "react";
import type { MissionDashboardConfig } from "../../core/domain/entities/MissionConfig";
import { missionRepository } from "../../infrastructure/dependencyInjection/container";
import type { ChartDataPoint } from "../../core/application/ports/IMissionRepository";

const withTimeout = async <T>(
  promise: Promise<T>,
  timeoutMs: number,
  message: string,
): Promise<T> => {
  let timeoutId: ReturnType<typeof setTimeout> | undefined;

  const timeout = new Promise<never>((_, reject) => {
    timeoutId = setTimeout(() => reject(new Error(message)), timeoutMs);
  });

  try {
    return await Promise.race([promise, timeout]);
  } finally {
    if (timeoutId) clearTimeout(timeoutId);
  }
};

export const useMissionData = () => {
  const [config, setConfig] = useState<MissionDashboardConfig | null>(null);
  const [optimizedConfig, setOptimizedConfig] =
    useState<Partial<MissionDashboardConfig> | null>(null);

  const [isLoading, setIsLoading] = useState<boolean>(false);

  const [isDataModified, setIsDataModified] = useState<boolean>(false);
  const [isOptimizing, setIsOptimizing] = useState<boolean>(false);
  const [isRecalculating, setIsRecalculating] = useState<boolean>(false);
  const [analysisError, setAnalysisError] = useState<string | null>(null);
  const [payloadSessionId, setPayloadSessionId] = useState<string | null>(null);

  const [chartData, setChartData] = useState<ChartDataPoint[]>([]);
  const [nominalSessionId, setNominalSessionId] = useState<string | null>(null);

  const loadConfig = useCallback(async () => {
    setIsLoading(true);
    try {
      const data = await withTimeout(
        missionRepository.getConfig(),
        20000,
        "Przekroczono limit czasu synchronizacji konfiguracji z serwerem.",
      );
      setConfig(data);
      setOptimizedConfig(null);
      setPayloadSessionId(null);
      setNominalSessionId(null);
      setChartData([]);
      setAnalysisError(null);
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
    setOptimizedConfig(null);
    setIsDataModified(true);
    setPayloadSessionId(null);
    setNominalSessionId(null);
    setChartData([]);
    setAnalysisError(null);

    try {
      await missionRepository.saveConfig(updatedConfig);

      const freshConfig = await withTimeout(
        missionRepository.getConfig(),
        20000,
        "Konfiguracja została zapisana, ale ponowne pobranie danych przekroczyło limit czasu.",
      );
      if (freshConfig) {
        setConfig(freshConfig);
      }
    } catch (error) {
      console.error("Błąd zapisu", error);
      setAnalysisError("Nie udało się zapisać konfiguracji na serwerze.");
    }
  };
  const resetMissionData = useCallback(() => {
    setConfig(null);
    setOptimizedConfig(null);
    setIsLoading(false);
    setIsDataModified(false);
    setIsOptimizing(false);
    setIsRecalculating(false);
    setAnalysisError(null);
    setPayloadSessionId(null);
    setNominalSessionId(null);
    setChartData([]);
  }, []);

  const runNominal = async (payloadId: string) => {
    setIsRecalculating(true);
    setAnalysisError(null);

    try {
      const { nominalSessionId: newNominalId, chartData: newChartData } =
        await withTimeout(
          missionRepository.recalculate(payloadId),
          60000,
          "Przekroczono limit czasu symulacji nominalnej.",
        );

      setNominalSessionId(newNominalId);
      setChartData(newChartData);
      setIsDataModified(false);
    } catch (error) {
      console.error("Błąd rekalkulacji", error);
      setAnalysisError(
        error instanceof Error
          ? error.message
          : "Nie udało się wykonać symulacji nominalnej.",
      );
    } finally {
      setIsRecalculating(false);
    }
  };

  const optimize = async () => {
    if (!config) return;

    setIsOptimizing(true);
    setAnalysisError(null);

    try {
      const { payloadSessionId: newPayloadId, updatedConfig } =
        await withTimeout(
          missionRepository.optimize(),
          60000,
          "Przekroczono limit czasu auto-optymalizacji.",
        );

      setPayloadSessionId(newPayloadId);
      setOptimizedConfig(updatedConfig);
      setIsDataModified(false);

      // Kończymy etap auto-optimize przed uruchomieniem nominalnej symulacji.
      // Dzięki temu UI nie wisi na spinnerze auto-optymalizacji, jeśli nominal trwa dłużej.
      setIsOptimizing(false);

      await runNominal(newPayloadId);
    } catch (error) {
      console.error("Błąd podczas optymalizacji:", error);
      setAnalysisError(
        error instanceof Error
          ? error.message
          : "Nie udało się wykonać auto-optymalizacji.",
      );
    } finally {
      setIsOptimizing(false);
    }
  };

  const recalculate = async () => {
    if (!payloadSessionId) return;
    await runNominal(payloadSessionId);
  };

  return {
    config,
    optimizedConfig,
    isLoading,
    isDataModified,
    setIsDataModified,
    isOptimizing,
    isRecalculating,
    analysisError,
    payloadSessionId,
    nominalSessionId,
    chartData,
    updateConfig,
    optimize,
    recalculate,
    loadConfig,
    resetMissionData,
  };
};
