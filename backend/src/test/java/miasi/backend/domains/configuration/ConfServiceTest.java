package miasi.backend.domains.configuration;

import miasi.backend.database.MissionPlansRepository;
import miasi.backend.database.ModuleRepository;
import miasi.backend.domains.configuration.missionPlan.MissionPlan;
import miasi.backend.domains.configuration.modules.Module;
import miasi.backend.domains.configuration.modules.ModuleType;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.stream.Stream;

@SpringBootTest
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ConfServiceTest {

  @Autowired
  private ConfService confService;

  @Autowired
  private MissionPlansRepository missionPlansRepository;

  @Autowired
  private ModuleRepository moduleRepository;

  @AfterAll
  void restoreDatabaseFiles(
      @Value("${database.path.realdb}")
      String changedCopy,
      @Value("${database.path.hardcopy}")
      String hardCopy
  ) throws IOException {
    Path sourceDir = Path.of(hardCopy);
    Path targetDir = Path.of(changedCopy);

    try (Stream<Path> files = Files.walk(sourceDir)) {
      files
          .filter(Files::isRegularFile)
          .filter(path -> path.toString().endsWith(".json"))
          .forEach(source -> {
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
    int id = confService.saveMissionPlan(plan);

    // then
    assert id >= 0;
    assert missionPlansRepository.findById(id) != null;
    assert getMissionPlansSize() == sizeBefore + 1;
  }

  @Test
  void addModule_shouldPersistInRepository() {
    // given
    Module module = new Module();
    int sizeBefore = moduleRepository.getModules().size();

    // when
    int id = confService.addModule(module);

    // then
    assert id >= 0;
    assert moduleRepository.getModules().size() == sizeBefore + 1;
    assert moduleRepository.getModules().get(id) != null;
  }

  @Test
  void addModuleType_shouldPersistInRepository() {
    // given
    ModuleType type = new ModuleType();
    int sizeBefore = moduleRepository.getModuleTypes().size();

    // when
    int id = confService.addModuleType(type);

    // then
    assert id >= 0;
    assert moduleRepository.getModuleTypes().size() == sizeBefore + 1;
    assert moduleRepository.getModuleTypes().get(id) != null;
  }

  private int getMissionPlansSize() {
    int i = 0;
    while (missionPlansRepository.findById(i) != null) {
      i++;
    }
    return i;
  }
}