package miasi.backend.authorization.application.common.exception;

public class AuthenticationFailedException extends AuthorizationException {
  public AuthenticationFailedException(String message) {
    super(message);
  }
}
