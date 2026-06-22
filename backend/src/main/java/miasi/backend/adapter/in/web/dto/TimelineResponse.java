package miasi.backend.adapter.in.web.dto;

import java.util.List;

public record TimelineResponse(List<ScheduledEventResponse> eventsSortedBySol) {}
