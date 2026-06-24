package miasi.backend.configuration.application.port.out;

public interface ConfigurationEventPublisherPort {
  void publishMissionPlanCreated(int missionPlanId);

  void publishMissionPlanUpdated(int missionPlanId);
}
