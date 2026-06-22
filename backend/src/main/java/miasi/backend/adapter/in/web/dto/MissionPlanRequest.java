package miasi.backend.adapter.in.web.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.util.List;

public record MissionPlanRequest(
    @NotNull(message = "Crew profile list is required") List<@Valid SexProfileRequest> crew,
    @PositiveOrZero(message = "Mission duration cannot be negative") int missionDurationSols,
    @NotNull(message = "Starting resources are required")
        List<@Valid ResourceRequest> startingResources,
    @NotNull(message = "Mission modules are required") List<@Valid ModuleRequest> modules,
    @PositiveOrZero(message = "Max starting weight cannot be negative") float maxStartingWeight) {}
