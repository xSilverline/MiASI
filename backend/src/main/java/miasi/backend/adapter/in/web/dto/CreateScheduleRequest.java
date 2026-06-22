package miasi.backend.adapter.in.web.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record CreateScheduleRequest(
    @NotBlank(message = "Mission plan id is required") String missionPlanId,
    @Min(value = 1, message = "Mission duration must be at least 1 sol") int durationSols) {}
