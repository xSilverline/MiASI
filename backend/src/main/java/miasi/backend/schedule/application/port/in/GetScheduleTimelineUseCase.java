package miasi.backend.schedule.application.port.in;

import miasi.backend.schedule.domain.model.MissionTimeline;

public interface GetScheduleTimelineUseCase {
  MissionTimeline getTimeline(String scheduleId);
}
