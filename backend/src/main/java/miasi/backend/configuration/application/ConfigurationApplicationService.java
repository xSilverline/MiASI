package miasi.backend.configuration.application;

import java.util.Optional;
import java.util.OptionalInt;
import lombok.RequiredArgsConstructor;
import miasi.backend.configuration.application.port.in.GetMissionPlanUseCase;
import miasi.backend.configuration.application.port.in.GetModuleCatalogUseCase;
import miasi.backend.configuration.application.port.in.ManageMissionPlanUseCase;
import miasi.backend.configuration.application.port.in.ManageModuleCatalogUseCase;
import miasi.backend.configuration.application.port.out.ConfigurationEventPublisherPort;
import miasi.backend.configuration.application.port.out.MissionPlanRepositoryPort;
import miasi.backend.configuration.application.port.out.ModuleRepositoryPort;
import miasi.backend.domains.configuration.missionPlan.MissionPlan;
import miasi.backend.domains.configuration.modules.Module;
import miasi.backend.domains.configuration.modules.ModuleCatalog;
import miasi.backend.domains.configuration.modules.ModuleType;

@RequiredArgsConstructor
public class ConfigurationApplicationService
    implements GetMissionPlanUseCase,
        ManageMissionPlanUseCase,
        GetModuleCatalogUseCase,
        ManageModuleCatalogUseCase {

  private final MissionPlanRepositoryPort missionPlansRepository;
  private final ModuleRepositoryPort moduleRepository;
  private final ConfigurationEventPublisherPort eventPublisher;

  @Override
  public int getPlansCount() {
    return missionPlansRepository.getPlansCount();
  }

  @Override
  public MissionPlan getDefaultMissionPlan() {
    return new MissionPlan();
  }

  @Override
  public Optional<MissionPlan> getMissionPlan(int missionId) {
    return missionPlansRepository.findById(missionId);
  }

  @Override
  public ModuleCatalog getModuleCatalog() {
    return moduleRepository.getCatalog();
  }

  @Override
  public int saveMissionPlan(MissionPlan missionPlan) {
    int id = missionPlansRepository.save(missionPlan);
    eventPublisher.publishMissionPlanCreated(id);

    return id;
  }

  @Override
  public OptionalInt overrideMissionPlan(int id, MissionPlan missionPlan) {
    int output = missionPlansRepository.replace(id, missionPlan);
    if (output == -1) {
      return OptionalInt.empty();
    }

    eventPublisher.publishMissionPlanUpdated(output);
    return OptionalInt.of(output);
  }

  @Override
  public int addModule(Module module) {
    return moduleRepository.add(module);
  }

  @Override
  public int addModuleType(ModuleType type) {
    return moduleRepository.add(type);
  }
}
