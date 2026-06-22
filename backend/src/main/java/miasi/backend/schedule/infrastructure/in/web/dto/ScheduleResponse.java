package miasi.backend.schedule.infrastructure.in.web.dto;

import java.util.List;
import miasi.backend.schedule.domain.model.ScheduleStatus;

public record ScheduleResponse(
    String id,
    String missionPlanId,
    int durationSols,
    ScheduleStatus status,
    List<ScheduledEventResponse> events) {}
