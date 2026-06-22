package miasi.backend.authorization.application.exception;

public class AuthenticationFailedException extends AuthorizationException {
  public AuthenticationFailedException(String message) {
    super(message);
  }
}
