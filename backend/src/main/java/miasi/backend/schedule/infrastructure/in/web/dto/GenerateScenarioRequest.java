package miasi.backend.schedule.infrastructure.in.web.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import miasi.backend.schedule.domain.model.DifficultyLevel;

public record GenerateScenarioRequest(
    @NotBlank(message = "Mission plan id is required") String missionPlanId,
    @Min(value = 1, message = "Mission duration must be at least 1 sol") int durationSols,
    @NotNull(message = "Difficulty level is required") DifficultyLevel difficulty) {}
