package miasi.backend.domains.configuration.ports;

import miasi.backend.domains.configuration.missionPlan.MissionPlan;

public interface IConfigurationEventPublisherPort {
    void publishMissionPlanCreated(MissionPlan plan);
}