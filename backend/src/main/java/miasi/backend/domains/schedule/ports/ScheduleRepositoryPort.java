package miasi.backend.domains.schedule.ports;

import java.util.Optional;
import miasi.backend.domains.schedule.MissionSchedule;
import miasi.backend.domains.schedule.ScenarioDraft;

public interface ScheduleRepositoryPort {

  Optional<MissionSchedule> findScheduleById(String scheduleId);

  MissionSchedule saveSchedule(MissionSchedule schedule);

  Optional<ScenarioDraft> findScenarioDraftById(String draftId);

  ScenarioDraft saveScenarioDraft(ScenarioDraft draft);
}
