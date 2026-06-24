package miasi.backend.database;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.IntStream;
import lombok.AccessLevel;
import lombok.Synchronized;
import lombok.experimental.FieldDefaults;
import miasi.backend.domains.schedule.MissionSchedule;
import miasi.backend.domains.schedule.ScenarioDraft;
import miasi.backend.domains.schedule.ports.ScheduleRepositoryPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

@Repository
@FieldDefaults(level = AccessLevel.PRIVATE)
public final class ScheduleJsonRepository implements ScheduleRepositoryPort {

  final JsonFileStorage<MissionSchedule> scheduleStorage =
      new JsonFileStorage<>(MissionSchedule.class);
  final JsonFileStorage<ScenarioDraft> scenarioDraftStorage =
      new JsonFileStorage<>(ScenarioDraft.class);
  final String schedulesFilePath;
  final String scenarioDraftsFilePath;
  final List<MissionSchedule> schedules;
  final List<ScenarioDraft> scenarioDrafts;

  public ScheduleJsonRepository(
      @Value("${database.filename.schedules}") String schedulesFilePath,
      @Value("${database.filename.scenario.drafts}") String scenarioDraftsFilePath) {
    this.schedulesFilePath = schedulesFilePath;
    this.scenarioDraftsFilePath = scenarioDraftsFilePath;
    this.schedules = loadList(scheduleStorage, schedulesFilePath);
    this.scenarioDrafts = loadList(scenarioDraftStorage, scenarioDraftsFilePath);
  }

  @Override
  @Synchronized
  public Optional<MissionSchedule> findScheduleById(String scheduleId) {
    return schedules.stream()
        .filter(schedule -> Objects.equals(schedule.getId(), scheduleId))
        .findFirst();
  }

  @Override
  @Synchronized
  public MissionSchedule saveSchedule(MissionSchedule schedule) {
    validateId(schedule == null ? null : schedule.getId(), "Schedule id is required");
    upsertSchedule(schedule);
    scheduleStorage.saveListToFile(schedules, schedulesFilePath);
    return schedule;
  }

  @Override
  @Synchronized
  public Optional<ScenarioDraft> findScenarioDraftById(String draftId) {
    return scenarioDrafts.stream()
        .filter(draft -> Objects.equals(draft.getId(), draftId))
        .findFirst();
  }

  @Override
  @Synchronized
  public ScenarioDraft saveScenarioDraft(ScenarioDraft draft) {
    validateId(draft == null ? null : draft.getId(), "Scenario draft id is required");
    upsertScenarioDraft(draft);
    scenarioDraftStorage.saveListToFile(scenarioDrafts, scenarioDraftsFilePath);
    return draft;
  }

  private void upsertSchedule(MissionSchedule schedule) {
    int index =
        IntStream.range(0, schedules.size())
            .filter(
                currentIndex ->
                    Objects.equals(schedules.get(currentIndex).getId(), schedule.getId()))
            .findFirst()
            .orElse(-1);
    if (index == -1) {
      schedules.add(schedule);
    } else {
      schedules.set(index, schedule);
    }
  }

  private void upsertScenarioDraft(ScenarioDraft draft) {
    int index =
        IntStream.range(0, scenarioDrafts.size())
            .filter(
                currentIndex ->
                    Objects.equals(scenarioDrafts.get(currentIndex).getId(), draft.getId()))
            .findFirst()
            .orElse(-1);
    if (index == -1) {
      scenarioDrafts.add(draft);
    } else {
      scenarioDrafts.set(index, draft);
    }
  }

  private <T> List<T> loadList(JsonFileStorage<T> storage, String filePath) {
    List<T> loaded = storage.loadListFromFile(filePath);
    return loaded == null ? new ArrayList<>() : new ArrayList<>(loaded);
  }

  private void validateId(String id, String message) {
    if (id == null || id.isBlank()) {
      throw new IllegalArgumentException(message);
    }
  }
}
