package miasi.backend.configuration.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalInt;
import java.util.stream.Stream;
import miasi.backend.configuration.adapter.out.persistence.json.MissionPlansRepository;
import miasi.backend.configuration.adapter.out.persistence.json.ModuleRepository;
import miasi.backend.domains.configuration.missionPlan.MissionPlan;
import miasi.backend.domains.configuration.modules.Module;
import miasi.backend.domains.configuration.modules.ModuleType;
import miasi.backend.domains.configuration.other.Resources;
import miasi.backend.sharedkernel.model.ModuleState;
import miasi.backend.sharedkernel.model.ResourceType;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ConfigurationApplicationServiceTest {

  @Autowired private ConfigurationApplicationService configurationApplicationService;

  @Autowired private MissionPlansRepository missionPlansRepository;

  @Autowired private ModuleRepository moduleRepository;

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
    Module module = new Module("test2", ModuleState.PARTIALLY_DAMAGED, ModuleType.genSample(), 12);
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
    Module module = new Module("test", ModuleState.PARTIALLY_DAMAGED, ModuleType.genSample(), 12);
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
  void addModuleType_shouldPersistInRepository() {
    // given
    ModuleType type = new ModuleType();
    int sizeBefore = moduleRepository.getModuleTypes().size();

    // when
    int id = configurationApplicationService.addModuleType(type);

    // then
    assertTrue(id >= 0);
    assertTrue(moduleRepository.getModuleTypes().size() >= sizeBefore);
    assertNotNull(moduleRepository.getModuleTypes().get(id));
    assertEquals(type, moduleRepository.getModuleTypes().get(id));
  }

  @Test
  void addModuleType_shouldOverrideSameName() {
    // given
    ModuleType type = ModuleType.genSample();
    type.setName("test");
    configurationApplicationService.addModuleType(type);
    int sizeBefore = moduleRepository.getModuleTypes().size();

    // when
    List<Resources> changedState = new ArrayList<>();
    changedState.add(new Resources(ResourceType.OXYGEN, 12));
    type.setResourceConsumption(changedState);
    int id = configurationApplicationService.addModuleType(type);

    // then
    assertTrue(id >= 0);
    assertEquals(sizeBefore, moduleRepository.getModuleTypes().size());
    assertNotNull(moduleRepository.getModuleTypes().get(id));
    assertEquals(changedState, moduleRepository.getModuleTypes().get(id).getResourceConsumption());
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
