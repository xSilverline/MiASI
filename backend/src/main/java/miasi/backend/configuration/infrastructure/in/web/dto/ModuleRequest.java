package miasi.backend.configuration.infrastructure.in.web.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import miasi.backend.common.domain.model.ModuleState;

public record ModuleRequest(
    @NotBlank(message = "Module name is required") String name,
    @NotNull(message = "Module state is required") ModuleState status,
    @NotNull(message = "Module type is required") @Valid ModuleTypeRequest type,
    @PositiveOrZero(message = "Module weight cannot be negative") float weight) {}
