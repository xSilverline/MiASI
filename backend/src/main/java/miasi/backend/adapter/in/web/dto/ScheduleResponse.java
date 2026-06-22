package miasi.backend.adapter.in.web.dto;

import java.util.List;
import miasi.backend.schedule.domain.ScheduleStatus;

public record ScheduleResponse(
    String id,
    String missionPlanId,
    int durationSols,
    ScheduleStatus status,
    List<ScheduledEventResponse> events) {}
