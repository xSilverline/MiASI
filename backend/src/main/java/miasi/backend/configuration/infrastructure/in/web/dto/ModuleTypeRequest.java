package miasi.backend.configuration.infrastructure.in.web.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record ModuleTypeRequest(
    @NotBlank(message = "Module type name is required") String name,
    @NotNull(message = "Resource consumption is required")
        List<@Valid ResourceRequest> resourceConsumption,
    @NotNull(message = "Resource production is required")
        List<@Valid ResourceRequest> resourceProduction) {}
