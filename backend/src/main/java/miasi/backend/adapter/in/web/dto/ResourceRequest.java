package miasi.backend.adapter.in.web.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import miasi.backend.sharedkernel.model.ResourceType;

public record ResourceRequest(
    @NotNull(message = "Resource type is required") ResourceType resourceType,
    @PositiveOrZero(message = "Resource quantity cannot be negative") float quantity) {}
