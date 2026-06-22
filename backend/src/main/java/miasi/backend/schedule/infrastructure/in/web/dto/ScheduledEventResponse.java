package miasi.backend.schedule.infrastructure.in.web.dto;

import miasi.backend.common.domain.model.ModuleState;
import miasi.backend.schedule.domain.model.EventType;
import miasi.backend.schedule.domain.model.ThreatType;

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
