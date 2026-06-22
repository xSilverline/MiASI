package miasi.backend.schedule.application.port.in;

import miasi.backend.domains.schedule.MissionTimeline;

public interface GetScheduleTimelineUseCase {
  MissionTimeline getTimeline(String scheduleId);
}
