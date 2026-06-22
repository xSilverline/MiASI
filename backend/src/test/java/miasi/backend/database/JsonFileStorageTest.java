package miasi.backend.database;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class JsonFileStorageTest {

  @Value("${database.path.realdb}")
  private String realDbPath;

  private final JsonFileStorage<TestData> storage = new JsonFileStorage<>(TestData.class);

  private static final String FILE_NAME = "test.json";

  @Test
  @Order(1)
  void saveFile() {
    File file = new File(realDbPath, FILE_NAME);
    file.getParentFile().mkdirs();

    TestData data = new TestData("Jan", 123);
    List<TestData> list = new ArrayList<>();
    list.add(data);

    storage.saveListToFile(list, file.getAbsolutePath());

    assertTrue(file.exists());
  }

  @Test
  @Order(2)
  void loadFile() {
    File file = new File(realDbPath, FILE_NAME);

    List<TestData> loaded = storage.loadListFromFile(
        file.getAbsolutePath()
    );

    assertEquals("Jan", loaded.getFirst().name());
    assertEquals(123, loaded.getFirst().value());
  }

  @AfterAll
  static void cleanup(@Value("${database.path.realdb}") String path) {
    File file = new File(path, FILE_NAME);
    file.delete();
  }

  record TestData(String name, int value) {
  }
}
