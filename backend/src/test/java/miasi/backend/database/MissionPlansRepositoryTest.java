package miasi.backend.database;

import miasi.backend.domains.configuration.missionPlan.MissionPlan;
import org.json.JSONException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class MissionPlansRepositoryTest {
  @Value("${database.filename.missions}")
  String path;

  @Autowired
  private ObjectMapper objectMapper;

  private MissionPlansRepository repository;

  @BeforeEach
  void setUp() {
    repository = new MissionPlansRepository(path);
  }

  @AfterEach
  void restoreFile(@Value("${database.path.hardcopy}") String hardCopy) throws IOException {
    Path source = Paths.get(hardCopy + path.substring(path.lastIndexOf("/")));
    Path target = Paths.get(path);

    Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
  }

  @Test
  void constructor_shouldLoadExistingPlans() {
    // given
    MissionPlan plan = new MissionPlan();
    int id = repository.save(plan);

    // when
    MissionPlansRepository newRepo = new MissionPlansRepository(path);

    // then
    assertNotNull(newRepo.findById(id));
  }

  @Test
  void findById_shouldReturnPlan() {
    // given
    MissionPlan plan = new MissionPlan();
    repository.save(plan);

    // when
    MissionPlan result = repository.findById(0);

    // then
    assertNotNull(result);
  }

  @Test
  void findById_shouldReturnNullWhenInvalidIndex() {
    // when
    MissionPlan result = repository.findById(999);

    // then
    assertNull(result);
  }

  @Test
  void save_shouldAddPlanAndPersist() throws JSONException {
    // given
    MissionPlan plan = new MissionPlan();

    // when
    int id = repository.save(plan);

    // then
    assertEquals(1, id);
    assertEquals(plan, repository.findById(1));

    // verify persistence
    MissionPlansRepository newRepo = new MissionPlansRepository(path);
    JSONAssert.assertEquals(
        objectMapper.writeValueAsString(plan),
        objectMapper.writeValueAsString(newRepo.findById(1)),
        true
    );
  }

  @Test
  void delete_shouldRemovePlan() {
    // given
    repository.save(new MissionPlan());
    repository.save(new MissionPlan());

    // when
    repository.delete(2);

    // then
    assertNull(repository.findById(2));
    assertNotNull(repository.findById(1)); // teraz drugi element przesunął się na index 0
  }
}