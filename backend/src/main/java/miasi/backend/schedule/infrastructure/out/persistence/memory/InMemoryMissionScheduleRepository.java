package miasi.backend.schedule.infrastructure.out.persistence.memory;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import miasi.backend.schedule.application.port.out.MissionScheduleRepositoryPort;
import miasi.backend.schedule.domain.model.MissionSchedule;
import org.springframework.stereotype.Component;

@Component
public class InMemoryMissionScheduleRepository implements MissionScheduleRepositoryPort {
  private final ConcurrentMap<String, MissionSchedule> schedules = new ConcurrentHashMap<>();

  @Override
  public void save(MissionSchedule schedule) {
    schedules.put(schedule.getId(), schedule);
  }

  @Override
  public Optional<MissionSchedule> findById(String scheduleId) {
    return Optional.ofNullable(schedules.get(scheduleId));
  }

  @Override
  public void delete(String scheduleId) {
    schedules.remove(scheduleId);
  }
}
