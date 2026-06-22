package miasi.backend.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.util.Map;
import miasi.backend.sharedkernel.model.ResourceType;

public record SexProfileRequest(
    @NotBlank(message = "Sex profile name is required") String name,
    @PositiveOrZero(message = "Population cannot be negative") int population,
    @NotNull(message = "Optimal demand is required") Map<ResourceType, Float> optimalDemand,
    @NotNull(message = "Minimal demand is required") Map<ResourceType, Float> minimalDemand) {}
