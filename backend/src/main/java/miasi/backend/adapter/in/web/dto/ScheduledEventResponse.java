package miasi.backend.adapter.in.web.dto;

import miasi.backend.schedule.domain.EventType;
import miasi.backend.schedule.domain.ThreatType;
import miasi.backend.sharedkernel.model.ModuleState;

public record ScheduledEventResponse(
    String id,
    EventType type,
    int sol,
    String description,
    ThreatType threatType,
    String affectedElement,
    Double impactValue,
    Integer durationSols,
    String impactUnit,
    DeliveryContentResponse content,
    String moduleId,
    ModuleState newState) {}
