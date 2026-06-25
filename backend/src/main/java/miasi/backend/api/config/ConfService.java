package miasi.backend.api.config;

import java.util.List;
import lombok.RequiredArgsConstructor;
import miasi.backend.domains.configuration.missionPlan.MissionPlan;
import miasi.backend.domains.configuration.modules.Module;
import miasi.backend.domains.configuration.ports.IConfigurationEventPublisherPort;
import miasi.backend.domains.configuration.ports.IMissionPlanRepositoryPort;
import miasi.backend.domains.configuration.ports.IModuleRepositoryPort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ConfService {

  private final IMissionPlanRepositoryPort missionPlansRepository;
  private final IModuleRepositoryPort moduleRepository;
  private final IConfigurationEventPublisherPort eventPublisher;

  public int getPlansCount() {
    return missionPlansRepository.getPlansCount();
  }

  public MissionPlan getDefaultMissionPlan() {
    return new MissionPlan();
  }

  public MissionPlan getMissionPlan(int missionId) {
    return missionPlansRepository.findById(missionId);
  }

  public List<Module> getModuleCatalog() {
    return moduleRepository.toJson();
  }

  public int saveMissionPlan(MissionPlan missionPlan) {
    int id = missionPlansRepository.save(missionPlan);
    eventPublisher.publishMissionPlanCreated(id, missionPlan);

    return id;
  }

  public Integer overrideMissionPlan(int id, MissionPlan missionPlan) {

    Integer output = missionPlansRepository.replace(id, missionPlan);
    output = (output != -1) ? output : null;

    if (output != null) {
      eventPublisher.publishMissionPlanCreated(output, missionPlan);
    }
    return output;
  }

  public int addModule(Module module) {
    return moduleRepository.add(module);
  }
}
