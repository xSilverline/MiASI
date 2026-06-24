package miasi.backend.schedule.application.port.out;

import java.util.Optional;
import miasi.backend.schedule.domain.model.MissionSchedule;

public interface MissionScheduleRepositoryPort {
  void save(MissionSchedule schedule);

  Optional<MissionSchedule> findById(String scheduleId);

  void delete(String scheduleId);
}
