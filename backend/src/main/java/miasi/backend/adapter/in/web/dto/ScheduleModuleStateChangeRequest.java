package miasi.backend.adapter.in.web.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import miasi.backend.sharedkernel.model.ModuleState;

public record ScheduleModuleStateChangeRequest(
    String id,
    @Min(value = 1, message = "Event sol must be at least 1") int sol,
    @NotBlank(message = "Description is required") String description,
    @NotBlank(message = "Module id is required") String moduleId,
    @NotNull(message = "New module state is required") ModuleState newState) {}
