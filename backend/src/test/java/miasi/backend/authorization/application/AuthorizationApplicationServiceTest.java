package miasi.backend.authorization.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import miasi.backend.authorization.application.common.exception.ActiveSessionAlreadyExistsException;
import miasi.backend.authorization.application.common.exception.AuthenticationFailedException;
import miasi.backend.authorization.application.common.exception.InvalidSessionTokenException;
import miasi.backend.authorization.application.port.out.PasswordVerifierPort;
import miasi.backend.authorization.application.port.out.UserRepositoryPort;
import miasi.backend.authorization.application.service.AuthorizationApplicationService;
import miasi.backend.authorization.domain.model.Identity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class AuthorizationApplicationServiceTest {

  private UserRepositoryPort repository;
  private AuthorizationApplicationService authorization;

  private final String login = "testUser";
  private final String password = "password123";
  private final String wrongPassword = "wrongPassword";

  private Identity identity;

  @BeforeEach
  void setUp() {
    repository = Mockito.mock(UserRepositoryPort.class);
    identity = new Identity(login, hashFor(password));
    when(repository.findAll()).thenReturn(List.of(identity));
    authorization = new AuthorizationApplicationService(repository, passwordVerifier());
  }

  @Test
  void shouldLoginSuccessfully() {
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
    AuthenticationFailedException exception =
        assertThrows(
            AuthenticationFailedException.class, () -> authorization.login(login, wrongPassword));

    assertEquals("Login failed.", exception.getMessage());
  }

  @Test
  void shouldFailLoginForUnknownUser() {
    // given
    AuthorizationApplicationService emptyAuth =
        new AuthorizationApplicationService(repository, passwordVerifier());

    // cache miss
    when(repository.findByLogin("unknown")).thenReturn(null);

    // when
    AuthenticationFailedException exception =
        assertThrows(
            AuthenticationFailedException.class, () -> emptyAuth.login("unknown", password));

    // then
    assertEquals("Login failed.", exception.getMessage());
  }

  @Test
  void shouldPreventSecondActiveSession() {
    // given
    authorization.login(login, password);

    // when
    ActiveSessionAlreadyExistsException exception =
        assertThrows(
            ActiveSessionAlreadyExistsException.class, () -> authorization.login(login, password));

    // then
    assertEquals("Access denied. Another active session already exists.", exception.getMessage());
  }

  @Test
  void shouldLogoutSuccessfully() {
    // given
    String token = authorization.login(login, password);

    // when
    authorization.logout(token);

    // then
    assertFalse(authorization.isAuthenticated(token));
  }

  @Test
  void shouldFailLogoutWithInvalidToken() {
    // given
    authorization.login(login, password);

    // when
    InvalidSessionTokenException exception =
        assertThrows(
            InvalidSessionTokenException.class, () -> authorization.logout("invalid-token"));

    // then
    assertEquals("Invalid session token.", exception.getMessage());
  }

  @Test
  void shouldReturnFalseForInvalidAuthenticationToken() {
    assertFalse(authorization.isAuthenticated("fake-token"));
  }

  @Test
  void shouldLoadUsersIntoCacheOnConstructor() {
    // given
    authorization.login(login, password);

    // then
    verify(repository, never()).findByLogin(login);
  }

  @Test
  void shouldLoadUserFromRepositoryWhenNotInCache() {
    // given
    UserRepositoryPort repo = Mockito.mock(UserRepositoryPort.class);
    Identity newIdentity = new Identity("newUser", hashFor(password));
    when(repo.findAll()).thenReturn(List.of());
    when(repo.findByLogin("newUser")).thenReturn(newIdentity);
    AuthorizationApplicationService auth =
        new AuthorizationApplicationService(repo, passwordVerifier());

    // when
    String token = auth.login("newUser", password);

    // then
    assertNotNull(token);
    verify(repo).findByLogin("newUser");
  }

  private PasswordVerifierPort passwordVerifier() {
    return (rawPassword, passwordHash) -> hashFor(rawPassword).equals(passwordHash);
  }

  private String hashFor(String rawPassword) {
    return rawPassword + "-hash";
  }
}
