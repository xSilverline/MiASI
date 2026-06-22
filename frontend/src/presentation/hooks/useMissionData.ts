import { useState, useEffect } from "react";
import type { MissionPlan } from "../../core/domain/entities/MissionPlan.ts";
import { missionRepository } from "../../infrastructure/dependencyInjection/container";

export const useMissionData = () => {
  const [config, setConfig] = useState<MissionPlan | null>(null);
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [isDataModified, setIsDataModified] = useState<boolean>(false);
  const [isRecalculating, setIsRecalculating] = useState<boolean>(false);

  useEffect(() => {
    let isMounted = true;
    const loadConfig = async () => {
      try {
        const data = await missionRepository.getConfig();
        if (isMounted) setConfig(data);
      } catch (error) {
        console.error("Błąd pobierania konfiguracji", error);
      } finally {
        if (isMounted) setIsLoading(false);
      }
    };
    void loadConfig();
    return () => {
      isMounted = false;
    };
  }, []);

  const updateConfig = async (newData: Partial<MissionPlan>) => {
    if (!config) return;
    const updatedConfig = { ...config, ...newData };
    setConfig(updatedConfig);
    setIsDataModified(true);

    try {
      await missionRepository.saveConfig(newData);
    } catch (error) {
      console.error("Błąd zapisu", error);
    }
  };

  const recalculate = async () => {
    setIsRecalculating(true);
    try {
      await missionRepository.recalculate();
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
    isRecalculating,
    updateConfig,
    recalculate,
    setIsDataModified,
  };
};
