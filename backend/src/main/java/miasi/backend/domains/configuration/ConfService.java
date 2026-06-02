package miasi.backend.domains.configuration;

import lombok.RequiredArgsConstructor;
import miasi.backend.domains.configuration.missionPlan.MissionPlan;
import miasi.backend.domains.configuration.missionPlan.MissionPlansRepository;
import miasi.backend.domains.configuration.modules.Module;
import miasi.backend.domains.configuration.modules.ModuleCatalog;
import miasi.backend.domains.configuration.modules.ModuleRepository;
import miasi.backend.domains.configuration.modules.ModuleType;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ConfService {

  private final MissionPlansRepository missionPlansRepository;
  private final ModuleRepository moduleRepository;

  public MissionPlan getDefaultMissionPlan() {
    return new MissionPlan();
  }

  public MissionPlan getMissionPlan(int missionId) {
    return missionPlansRepository.findById(missionId);
  }

  public ModuleCatalog getModuleCatalog() {
    return moduleRepository.toJson();
  }

  public int saveMissionPlan(MissionPlan missionPlan) {
    return missionPlansRepository.save(missionPlan);
  }

  public int addModule(Module module) {
    return moduleRepository.add(module);
  }

  public int addModuleType(ModuleType type) {
    return moduleRepository.add(type);
  }
}