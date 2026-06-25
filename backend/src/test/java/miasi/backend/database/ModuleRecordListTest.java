package miasi.backend.database;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ModuleRecordListTest {

  private JsonFileStorage database;
  private ModuleRecordList<String> recordList;

  @BeforeEach
  void setUp() {
    // given
    database = mock(JsonFileStorage.class);
    recordList = new ModuleRecordList<>(List.of("A", "B"), "test.json", database);
  }

  @Test
  void add_shouldAddElementToList() {
    // when
    recordList.add("C");

    // then
    assertTrue(recordList.getObjects().contains("C"));
    assertEquals(3, recordList.getObjects().size());
  }

  @Test
  void save_shouldCallDatabaseSaveToFile() {
    // when
    recordList.save();

    // then
    verify(database, times(1)).saveListToFile(recordList.getObjects(), "test.json");
  }

  @Test
  void remove_shouldRemoveElementFromList() {
    // when
    recordList.remove("A");

    // then
    assertFalse(recordList.getObjects().contains("A"));
    assertEquals(1, recordList.getObjects().size());
    assertTrue(recordList.getObjects().contains("B"));
  }
}
