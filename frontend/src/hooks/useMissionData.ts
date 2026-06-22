import { useState, useEffect } from "react";
import {
  missionAdapter,
  type MissionDashboardConfig,
} from "../infrastructure/MissionAdapter";

export const useMissionData = () => {
  const [config, setConfig] = useState<MissionDashboardConfig | null>(null);
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [isDataModified, setIsDataModified] = useState<boolean>(false);
  const [isRecalculating, setIsRecalculating] = useState<boolean>(false);

  useEffect(() => {
    missionAdapter
      .getConfig()
      .then((data) => {
        setConfig(data);
      })
      .catch((error) => {
        console.error("Błąd pobierania konfiguracji", error);
      })
      .finally(() => {
        setIsLoading(false);
      });
  }, []);

  const updateConfig = async (newData: Partial<MissionDashboardConfig>) => {
    if (!config) return;

    const updatedConfig = { ...config, ...newData };
    setConfig(updatedConfig);
    setIsDataModified(true);

    try {
      await missionAdapter.saveConfig(newData);
    } catch (error) {
      console.error("Błąd zapisu", error);
    }
  };

  const recalculate = async () => {
    setIsRecalculating(true);
    try {
      await missionAdapter.recalculate();
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
