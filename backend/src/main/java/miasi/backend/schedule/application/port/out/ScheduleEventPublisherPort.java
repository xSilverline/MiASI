package miasi.backend.schedule.application.port.out;

import miasi.backend.schedule.domain.model.MissionSchedule;
import miasi.backend.schedule.domain.model.ScheduledEvent;

public interface ScheduleEventPublisherPort {
  ScheduleEventPublisherPort NO_OP =
      new ScheduleEventPublisherPort() {
        @Override
        public void publishScheduleCreated(MissionSchedule schedule) {}

        @Override
        public void publishScheduleUpdated(MissionSchedule schedule) {}

        @Override
        public void publishScheduledEventAdded(String scheduleId, ScheduledEvent event) {}
      };

  void publishScheduleCreated(MissionSchedule schedule);

  void publishScheduleUpdated(MissionSchedule schedule);

  void publishScheduledEventAdded(String scheduleId, ScheduledEvent event);
}
