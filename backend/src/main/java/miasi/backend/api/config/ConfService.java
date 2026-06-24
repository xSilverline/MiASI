package miasi.backend.api.config;

import lombok.RequiredArgsConstructor;
import miasi.backend.database.EventCatalogRepository;
import miasi.backend.domains.configuration.missionPlan.MissionPlan;
import miasi.backend.domains.configuration.modules.Module;
import miasi.backend.domains.configuration.ports.IConfigurationEventPublisherPort;
import miasi.backend.domains.configuration.ports.IMissionPlanRepositoryPort;
import miasi.backend.domains.configuration.ports.IModuleRepositoryPort;
import miasi.backend.domains.schedule.EventDefinition;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ConfService {

  private final IMissionPlanRepositoryPort missionPlansRepository;
  private final IModuleRepositoryPort moduleRepository;
  private final EventCatalogRepository eventCatalogRepository;
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

  public List<EventDefinition> getEventCatalog() {
    return eventCatalogRepository.findAll();
  }

  public EventDefinition addEventDefinition(EventDefinition event) {
    return eventCatalogRepository.save(event);
  }

  public EventDefinition updateEventDefinition(String eventId, EventDefinition event) {
    return eventCatalogRepository.update(eventId, event);
  }

  public boolean deleteEventDefinition(String eventId) {
    return eventCatalogRepository.delete(eventId);
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
