package miasi.backend.domains.configuration.ports;

import miasi.backend.domains.configuration.missionPlan.MissionPlan;

public interface IMissionPlanRepositoryPort {
    MissionPlan findById(int missionId);
    int save(MissionPlan plan);
    void delete(int missionId);
}