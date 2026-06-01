import React, { useState } from "react";
import earthBg from "../../assets/earth.png";
import marsBg from "../../assets/mars.png";

import { ResourceConfigStep } from "./ResourceConfigView";
import { ModulesConfigStep } from "./ModulesConfigView";
import { AddModuleStep } from "./AddEditModuleView";
import { type ModuleData, MOCK_MODULES } from "../../types/module";

interface ConfigCreatorViewProps {
  onFinish: () => void;
}

export const ConfigCreatorView: React.FC<ConfigCreatorViewProps> = ({
  onFinish,
}) => {
  const [step, setStep] = useState<number>(1);
  const [modules, setModules] = useState<ModuleData[]>(MOCK_MODULES);
  const [editingModuleId, setEditingModuleId] = useState<string | null>(null);

  const getStepTitle = () => {
    switch (step) {
      case 1:
        return "Konfiguracja Zużycia Zasobów";
      case 2:
      case 3:
      case 4:
        return "Konfiguracja Modułów";
      default:
        return "";
    }
  };

  const handleAddModule = (newModule: Omit<ModuleData, "id">) => {
    const moduleWithId: ModuleData = { ...newModule, id: crypto.randomUUID() };
    setModules([...modules, moduleWithId]);
    setStep(2); // Wracamy do listy
  };

  const handleEditModule = (updatedModule: ModuleData) => {
    setModules(
      modules.map((m) => (m.id === updatedModule.id ? updatedModule : m)),
    );
    setStep(2);
  };

  const handleDeleteModule = (id: string) => {
    setModules(modules.filter((m) => m.id !== id));
  };

  const startEditing = (id: string) => {
    setEditingModuleId(id);
    setStep(4);
  };

  const moduleToEdit = modules.find((m) => m.id === editingModuleId);

  return (
    <div className="h-screen w-screen bg-mars-background font-sans flex flex-col items-center justify-center relative overflow-hidden text-slate-100 select-none">
      <div className="absolute left-0 top-1/2 -translate-x-1/2 -translate-y-1/2 w-[45vw] h-[90vh] opacity-40 pointer-events-none bg-radial from-blue-900/20 to-transparent rounded-full blur-2xl lg:opacity-100" />
      <img
        src={earthBg}
        className="absolute left-0 top-1/2 -translate-x-1/2 -translate-y-1/2 h-[90vh] w-auto object-contain pointer-events-none select-none hidden lg:block opacity-25"
        alt="Earth"
      />

      <div className="absolute right-0 top-1/2 translate-x-1/2 -translate-y-1/2 w-[45vw] h-[90vh] opacity-40 pointer-events-none bg-radial from-mars-orange/10 to-transparent rounded-full blur-2xl lg:opacity-100" />
      <img
        src={marsBg}
        className="absolute right-0 top-1/2 translate-x-1/2 -translate-y-1/2 h-[90vh] w-auto object-contain pointer-events-none select-none hidden lg:block opacity-25"
        alt="Mars"
      />

      <div className="flex flex-col items-center z-10 w-full max-w-2xl px-6">
        <h1 className="text-xl md:text-2xl font-bold tracking-widest uppercase mb-8 flex gap-3">
          <span className="text-gradient-mars">Kreator Konfiguracji</span>
        </h1>

        <div className="bg-mars-itemBackground px-8 py-4 rounded-2xl mb-10 shadow-md">
          <span className="tracking-widest text-xs md:text-sm font-medium text-slate-200 uppercase transition-all">
            {getStepTitle()}
          </span>
        </div>

        {step === 1 && <ResourceConfigStep onNext={() => setStep(2)} />}

        {step === 2 && (
          <ModulesConfigStep
            modules={modules}
            onPrev={() => setStep(1)}
            onFinish={onFinish}
            onAddModule={() => setStep(3)}
            onEditModule={startEditing}
            onDeleteModule={handleDeleteModule}
          />
        )}

        {step === 3 && (
          <AddModuleStep
            mode="add"
            onCancel={() => setStep(2)}
            onSave={(data) => handleAddModule(data as Omit<ModuleData, "id">)}
          />
        )}

        {step === 4 && moduleToEdit && (
          <AddModuleStep
            mode="edit"
            initialData={moduleToEdit}
            onCancel={() => setStep(2)}
            onSave={(data) => handleEditModule(data as ModuleData)}
          />
        )}
      </div>
    </div>
  );
};
