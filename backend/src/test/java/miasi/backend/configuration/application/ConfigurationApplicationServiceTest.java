package miasi.backend.configuration.application;

import miasi.backend.common.domain.model.ModuleState;
import miasi.backend.configuration.application.service.ConfigurationApplicationService;
import miasi.backend.configuration.domain.model.MissionPlan;
import miasi.backend.configuration.domain.model.Module;
import miasi.backend.configuration.infrastructure.out.persistence.json.MissionPlansRepository;
import miasi.backend.configuration.infrastructure.out.persistence.json.ModuleRepository;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.OptionalInt;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ConfigurationApplicationServiceTest {

  @Autowired
  private ConfigurationApplicationService configurationApplicationService;

  @Autowired
  private MissionPlansRepository missionPlansRepository;

  @Autowired
  private ModuleRepository moduleRepository;

  @AfterAll
  void restoreDatabaseFiles() throws IOException {
    Path sourceDir = Path.of("src/test/resources/database/databaseHardCopy");
    Path targetDir = Path.of("src/test/resources/database");

    try (Stream<Path> files = Files.walk(sourceDir)) {
      files
          .filter(Files::isRegularFile)
          .filter(path -> path.toString().endsWith(".json"))
          .forEach(
              source -> {
                try {
                  Path relative = sourceDir.relativize(source);
                  Path target = targetDir.resolve(relative);

                  Files.createDirectories(target.getParent());

                  Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                  throw new UncheckedIOException(e);
                }
              });
    }
  }

  @Test
  void saveMissionPlan_shouldPersistInRepository() {
    // given
    MissionPlan plan = new MissionPlan();
    int sizeBefore = getMissionPlansSize();

    // when
    int id = configurationApplicationService.saveMissionPlan(plan);

    // then
    assertTrue(id >= 0);
    assertTrue(missionPlansRepository.findById(id).isPresent());
    assertEquals(sizeBefore + 1, getMissionPlansSize());
  }

  @Test
  void overrideMissionPlan_shouldOverridePlan() {
    // given
    int sizeBefore = getMissionPlansSize();
    int solsDuration = Integer.MAX_VALUE;
    MissionPlan plan = new MissionPlan();
    plan.setMissionDurationSols(solsDuration);

    // when
    int placeAt = 0;
    OptionalInt id = configurationApplicationService.overrideMissionPlan(placeAt, plan);

    // then
    assertTrue(id.isPresent());
    assertEquals(placeAt, id.getAsInt());
    assertTrue(missionPlansRepository.findById(placeAt).isPresent());
    assertEquals(sizeBefore, getMissionPlansSize());
    assertEquals(
        solsDuration,
        missionPlansRepository.findById(placeAt).orElseThrow().getMissionDurationSols());
  }

  @Test
  void overrideMissionPlan_shouldReturnEmptyForInvalidId() {
    // given
    int sizeBefore = getMissionPlansSize();
    MissionPlan plan = new MissionPlan();

    // when
    int placeAt = sizeBefore + 2;
    OptionalInt id = configurationApplicationService.overrideMissionPlan(placeAt, plan);

    // then
    assertTrue(id.isEmpty());
    assertFalse(configurationApplicationService.getMissionPlan(placeAt).isPresent());
    assertEquals(sizeBefore, getMissionPlansSize());
  }

  @Test
  void addModule_shouldPersistInRepository() {
    // given
    Module module = new Module();
    module.setName("test2");
    module.setStatus(ModuleState.PARTIALLY_DAMAGED);
    module.setWeight(12);
    int sizeBefore = moduleRepository.getModules().size();

    // when
    int id = configurationApplicationService.addModule(module);

    // then
    assertTrue(id >= 0);
    assertTrue(moduleRepository.getModules().size() >= sizeBefore);
    assertNotNull(moduleRepository.getModules().get(id));
    assertEquals(module, moduleRepository.getModules().get(id));
  }

  @Test
  void addModule_shouldOverrideSameName() {
    // given
    Module module = new Module();
    module.setName("test");
    module.setStatus(ModuleState.PARTIALLY_DAMAGED);
    module.setWeight(12);
    configurationApplicationService.addModule(module);
    int sizeBefore = moduleRepository.getModules().size();

    // when
    ModuleState changedState = ModuleState.DESTROYED;
    module.setStatus(changedState);
    int id = configurationApplicationService.addModule(module);

    // then
    assertTrue(id >= 0);
    assertEquals(sizeBefore, moduleRepository.getModules().size());
    assertNotNull(moduleRepository.getModules().get(id));
    assertEquals(changedState, moduleRepository.getModules().get(id).getStatus());
  }

  @Test
  void getPlansCount() {
    assertEquals(
        missionPlansRepository.getPlansCount(), configurationApplicationService.getPlansCount());
  }

  private int getMissionPlansSize() {
    int i = 0;
    while (missionPlansRepository.findById(i).isPresent()) {
      i++;
    }
    return i;
  }
}
