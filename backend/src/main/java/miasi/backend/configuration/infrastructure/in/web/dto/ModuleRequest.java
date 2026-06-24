package miasi.backend.configuration.infrastructure.in.web.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import miasi.backend.common.domain.model.ModuleState;
import miasi.backend.configuration.domain.model.ModuleCategory;

import java.util.List;

public record ModuleRequest(
    @NotBlank(message = "Module name is required") String name,
    @NotNull(message = "Module state is required") ModuleState status,
    @NotNull(message = "Module category is required") ModuleCategory category,
    @PositiveOrZero(message = "Module weight cannot be negative") float weight,
    @NotNull(message = "Resource consumption is required")
    List<@Valid ResourceRequest> resourceConsumption,
    @NotNull(message = "Resource production is required")
    List<@Valid ResourceRequest> resourceProduction) {
}
