package miasi.backend.schedule.infrastructure.in.web.dto;

import java.util.List;
import miasi.backend.schedule.domain.model.DifficultyLevel;
import miasi.backend.schedule.domain.model.ScenarioGenerationMode;

public record ScenarioDraftResponse(
    String id,
    String missionPlanId,
    int durationSols,
    ScenarioGenerationMode mode,
    DifficultyLevel difficulty,
    List<ScheduledEventResponse> proposedEvents) {}
