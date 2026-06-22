package miasi.backend.adapter.in.web.dto;

import java.util.List;
import miasi.backend.schedule.domain.DifficultyLevel;
import miasi.backend.schedule.domain.ScenarioGenerationMode;

public record ScenarioDraftResponse(
    String id,
    String missionPlanId,
    int durationSols,
    ScenarioGenerationMode mode,
    DifficultyLevel difficulty,
    List<ScheduledEventResponse> proposedEvents) {}
