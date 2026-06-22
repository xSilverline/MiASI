package miasi.backend.schedule.infrastructure.in.web.dto;

import java.util.List;

public record TimelineResponse(List<ScheduledEventResponse> eventsSortedBySol) {}
