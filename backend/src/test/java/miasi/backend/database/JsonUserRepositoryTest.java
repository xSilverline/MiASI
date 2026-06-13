package miasi.backend.database;

import miasi.backend.domains.authorization.Identity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JsonUserRepositoryTest {

  private JsonFileStorage<Identity> database;
  private JsonUserRepository repository;


  private Identity user1;
  private Identity user2;


  @BeforeEach
  void setUp() throws Exception {
    database = mock(JsonFileStorage.class);
    repository = new JsonUserRepository();

    var field = JsonUserRepository.class.getDeclaredField("database");
    field.setAccessible(true);
    field.set(repository, database);

    repository.filePath = "test.json";
    user1 = new Identity("john", "hash1");
    user2 = new Identity("alice", "hash2");
  }


  @Test
  void findByLogin_shouldReturnUser() {
    // given
    when(database.loadListFromFile("test.json"))
        .thenReturn(List.of(user1, user2));

    // when
    Identity result = repository.findByLogin("john");

    // then
    assertNotNull(result);
    assertEquals("john", result.getLogin());
  }

  @Test
  void findByLogin_shouldIgnoreCase() {
    // given
    when(database.loadListFromFile("test.json"))
        .thenReturn(List.of(user1));

    // when
    Identity result = repository.findByLogin("JOHN");

    // then
    assertNotNull(result);
    assertEquals("john", result.getLogin());
  }

  @Test
  void findByLogin_shouldReturnNullWhenMissing() {
    // given
    when(database.loadListFromFile("test.json"))
        .thenReturn(List.of(user1));

    // when
    Identity result = repository.findByLogin("unknown");

    // then
    assertNull(result);
  }

  @Test
  void findAll_shouldReturnUsers() {
    // given
    when(database.loadListFromFile("test.json"))
        .thenReturn(List.of(user1, user2));

    // when
    var result = repository.findAll();

    // then
    assertEquals(2, result.size());
    assertTrue(result.contains(user1));
    assertTrue(result.contains(user2));
  }

  @Test
  void findAll_shouldReturnEmptyListWhenDatabaseReturnsNull() {
    // given
    when(database.loadListFromFile("test.json"))
        .thenReturn(null);

    // when
    var result = repository.findAll();

    // then
    assertNotNull(result);
    assertTrue(result.isEmpty());
  }

  @Test
  void exists_shouldReturnTrueWhenUserExists() {
    // given
    when(database.loadListFromFile("test.json"))
        .thenReturn(List.of(user1));

    // when
    boolean result = repository.exists("john");

    // then
    assertTrue(result);
  }

  @Test
  void exists_shouldReturnFalseWhenUserMissing() {
    // given
    when(database.loadListFromFile("test.json"))
        .thenReturn(List.of(user1));

    // when
    boolean result = repository.exists("bob");

    // then
    assertFalse(result);
  }
}