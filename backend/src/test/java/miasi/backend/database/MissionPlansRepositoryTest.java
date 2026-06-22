package miasi.backend.database;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Path;
import java.util.List;
import miasi.backend.configuration.adapter.out.persistence.json.MissionPlansRepository;
import miasi.backend.domains.configuration.missionPlan.MissionPlan;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class MissionPlansRepositoryTest {
  @TempDir private Path tempDir;
  private Path filePath;
  private MissionPlansRepository repository;

  @BeforeEach
  void setUp() {
    filePath = tempDir.resolve("missionPlans.json");
    new JsonFileStorage<>(MissionPlan.class)
        .saveListToFile(List.of(new MissionPlan()), filePath.toString());
    repository = new MissionPlansRepository(filePath.toString());
  }

  @Test
  void constructor_shouldLoadExistingPlans() {
    // given
    MissionPlan plan = new MissionPlan();
    int id = repository.save(plan);

    // when
    MissionPlansRepository newRepo = new MissionPlansRepository(filePath.toString());

    // then
    assertTrue(newRepo.findById(id).isPresent());
  }

  @Test
  void findById_shouldReturnPlan() {
    // given
    MissionPlan plan = new MissionPlan();
    repository.save(plan);

    // when
    MissionPlan result = repository.findById(0).orElseThrow();

    // then
    assertNotNull(result);
  }

  @Test
  void findById_shouldReturnEmptyWhenInvalidIndex() {
    // when
    boolean result = repository.findById(999).isPresent();

    // then
    assertFalse(result);
  }

  @Test
  void save_shouldAddPlanAndPersist() {
    // given
    MissionPlan plan = new MissionPlan();
    plan.setMissionDurationSols(360);

    // when
    int id = repository.save(plan);

    // then
    assertEquals(1, id);
    assertEquals(360, repository.findById(1).orElseThrow().getMissionDurationSols());

    // verify persistence
    MissionPlansRepository newRepo = new MissionPlansRepository(filePath.toString());
    assertEquals(360, newRepo.findById(1).orElseThrow().getMissionDurationSols());
  }

  @Test
  void replace_shouldPersistWhenRepositoryIsReloaded() {
    // given
    MissionPlan plan = new MissionPlan();
    plan.setMissionDurationSols(420);

    // when
    int id = repository.replace(0, plan);

    // then
    assertEquals(0, id);
    MissionPlansRepository reloadedRepository = new MissionPlansRepository(filePath.toString());
    assertEquals(420, reloadedRepository.findById(0).orElseThrow().getMissionDurationSols());
  }

  @Test
  void delete_shouldRemovePlanAndPersist() {
    // given
    repository.save(new MissionPlan());
    repository.save(new MissionPlan());

    // when
    repository.delete(2);

    // then
    assertFalse(repository.findById(2).isPresent());
    assertTrue(repository.findById(1).isPresent());

    MissionPlansRepository reloadedRepository = new MissionPlansRepository(filePath.toString());
    assertFalse(reloadedRepository.findById(2).isPresent());
    assertTrue(reloadedRepository.findById(1).isPresent());
  }
}
