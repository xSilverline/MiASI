package miasi.backend.api.jsons;

import miasi.backend.domains.schedule.enums.DifficultyLevel;

public record GenerateScenarioRequest(
    String missionPlanId, int durationSols, DifficultyLevel difficulty) {}
