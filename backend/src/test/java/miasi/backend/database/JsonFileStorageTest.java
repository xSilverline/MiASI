package miasi.backend.database;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class JsonFileStorageTest {

  @Value("${database.path.realdb}")
  private String realDbPath;

  private final JsonFileStorage storage = new JsonFileStorage();

  private static final String FILE_NAME = "test.json";

  @Test
  @Order(1)
  void saveFile() {
    File file = new File(realDbPath, FILE_NAME);
    file.getParentFile().mkdirs();

    TestData data = new TestData("Jan", 123);

    storage.saveToFile(data, file.getAbsolutePath());

    assertTrue(file.exists());
  }

  @Test
  @Order(2)
  void loadFile() {
    File file = new File(realDbPath, FILE_NAME);

    TestData loaded = storage.loadFromFile(
        file.getAbsolutePath()
    );

    assertEquals("Jan", loaded.name());
    assertEquals(123, loaded.value());
  }

  @AfterAll
  static void cleanup(@Value("${database.path.realdb}") String path) {
    File file = new File(path, FILE_NAME);
    file.delete();
  }

  record TestData(String name, int value) {
  }
}
