package miasi.backend.authorization.application;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import miasi.backend.authorization.application.exception.ActiveSessionAlreadyExistsException;
import miasi.backend.authorization.application.exception.AuthenticationFailedException;
import miasi.backend.authorization.application.exception.InvalidSessionTokenException;
import miasi.backend.authorization.application.port.in.LoginUseCase;
import miasi.backend.authorization.application.port.in.LogoutUseCase;
import miasi.backend.authorization.application.port.in.VerifySessionUseCase;
import miasi.backend.authorization.application.port.out.PasswordVerifierPort;
import miasi.backend.domains.authorization.IUserRepository;
import miasi.backend.domains.authorization.Identity;
import miasi.backend.domains.authorization.Session;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class AuthorizationApplicationService
    implements LoginUseCase, VerifySessionUseCase, LogoutUseCase {

  final IUserRepository repository;
  final PasswordVerifierPort passwordVerifier;
  final ConcurrentHashMap<String, Identity> cache = new ConcurrentHashMap<>();
  Session activeSession;

  public AuthorizationApplicationService(
      IUserRepository repository, PasswordVerifierPort passwordVerifier) {
    this.repository = repository;
    this.passwordVerifier = passwordVerifier;
    Collection<Identity> identities = repository.findAll();
    if (identities != null) {
      identities.forEach(identity -> cache.put(identity.getLogin(), identity));
    }
  }

  @Override
  public synchronized String login(String login, String password) {
    if (activeSession != null) {
      throw new ActiveSessionAlreadyExistsException(
          "Access denied. Another active session already exists.");
    }

    Identity identity = findIdentity(login);
    if (identity == null || !passwordVerifier.matches(password, identity.getPasswordHash())) {
      throw new AuthenticationFailedException("Login failed.");
    }

    activeSession = new Session(login);
    return activeSession.getSessionToken();
  }

  @Override
  public synchronized void logout(String token) {
    if (activeSession != null && activeSession.getSessionToken().equals(token)) {
      activeSession = null;
      return;
    }

    throw new InvalidSessionTokenException("Invalid session token.");
  }

  @Override
  public boolean isAuthenticated(String token) {
    return activeSession != null && activeSession.getSessionToken().equals(token);
  }

  private Identity findIdentity(String login) {
    Identity identity = cache.get(login);
    if (identity != null) {
      return identity;
    }

    identity = repository.findByLogin(login);
    if (identity != null) {
      cache.put(login, identity);
    }
    return identity;
  }
}
