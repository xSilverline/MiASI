package miasi.backend.api.jsons;

import miasi.backend.domains.schedule.ModuleState;

public record ScheduleModuleStateChangeRequest(
    String id, int sol, String description, String moduleId, ModuleState newState) {}
