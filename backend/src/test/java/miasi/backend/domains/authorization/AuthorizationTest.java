package miasi.backend.domains.authorization;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthorizationTest {

  private IUserRepository repository;
  private Authorization authorization;

  private final String login = "testUser";
  private final String password = "password123";
  private final String wrongPassword = "wrongPassword";

  private Identity identity;


  @BeforeEach
  void setUp() {
    repository = Mockito.mock(IUserRepository.class);
    String hash = BCrypt.hashpw(password, BCrypt.gensalt());
    identity = new Identity(login, hash);
    when(repository.findAll()).thenReturn(List.of(identity));
    authorization = new Authorization(repository);
  }


  @Test
  void shouldLoginSuccessfully() throws Exception {
    // when
    String token = authorization.login(login, password);

    // then
    assertNotNull(token);
    assertTrue(authorization.isAuthenticated(token));

    verify(repository, never()).findByLogin(login);
  }


  @Test
  void shouldFailLoginWithWrongPassword() {
    // when + then
    Exception exception = assertThrows(
        Exception.class,
        () -> authorization.login(login, wrongPassword)
    );

    assertEquals("Login failed.", exception.getMessage());
  }


  @Test
  void shouldFailLoginForUnknownUser() {
    // given
    Authorization emptyAuth = new Authorization(repository);

    // cache miss
    when(repository.findByLogin("unknown"))
        .thenReturn(null);

    // when
    Exception exception = assertThrows(
        Exception.class,
        () -> emptyAuth.login("unknown", password)
    );

    // then
    assertEquals("Login failed.", exception.getMessage());
  }


  @Test
  void shouldPreventSecondActiveSession() throws Exception {
    // given
    authorization.login(login, password);

    // when
    Exception exception = assertThrows(
        Exception.class,
        () -> authorization.login(login, password)
    );

    // then
    assertEquals(
        "Access denied. Another active session already exists.",
        exception.getMessage()
    );
  }


  @Test
  void shouldLogoutSuccessfully() throws Exception {
    // given
    String token = authorization.login(login, password);

    // when
    authorization.logout(token);

    // then
    assertFalse(authorization.isAuthenticated(token));
  }


  @Test
  void shouldFailLogoutWithInvalidToken() throws Exception {
    // given
    authorization.login(login, password);

    // when
    Exception exception = assertThrows(
        Exception.class,
        () -> authorization.logout("invalid-token")
    );

    // then
    assertEquals(
        "Invalid session token.",
        exception.getMessage()
    );
  }


  @Test
  void shouldReturnFalseForInvalidAuthenticationToken() {
    assertFalse(
        authorization.isAuthenticated("fake-token")
    );
  }


  @Test
  void shouldLoadUsersIntoCacheOnConstructor() throws Exception {
    // given
    authorization.login(login, password);

    // then
    verify(repository, never()).findByLogin(login);
  }


  @Test
  void shouldLoadUserFromRepositoryWhenNotInCache() throws Exception {
    // given
    IUserRepository repo = Mockito.mock(IUserRepository.class);
    String hash = BCrypt.hashpw(password, BCrypt.gensalt());
    Identity newIdentity = new Identity("newUser", hash);
    when(repo.findAll()).thenReturn(List.of());
    when(repo.findByLogin("newUser")).thenReturn(newIdentity);
    Authorization auth = new Authorization(repo);

    // when
    String token = auth.login("newUser", password);

    // then
    assertNotNull(token);
    verify(repo).findByLogin("newUser");
  }
}