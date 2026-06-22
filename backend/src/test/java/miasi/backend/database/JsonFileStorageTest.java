package miasi.backend.database;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

class JsonFileStorageTest {
  @TempDir private Path tempDir;

  private final JsonFileStorage<TestData> storage = new JsonFileStorage<>(TestData.class);

  @Test
  void saveFile_shouldCreateParentDirectories() {
    Path file = tempDir.resolve("nested").resolve("test.json");
    TestData data = new TestData("Jan", 123);

    storage.saveListToFile(List.of(data), file.toString());

    assertTrue(file.toFile().exists());
  }

  @Test
  void loadFile_shouldReadSavedList() {
    Path file = tempDir.resolve("test.json");
    TestData data = new TestData("Jan", 123);
    storage.saveListToFile(List.of(data), file.toString());

    List<TestData> loaded = storage.loadListFromFile(file.toString());

    assertEquals("Jan", loaded.getFirst().name());
    assertEquals(123, loaded.getFirst().value());
  }

  record TestData(String name, int value) {}
}
