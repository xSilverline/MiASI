package miasi.backend.authorization.application.common.exception;

public class InvalidSessionTokenException extends AuthorizationException {
  public InvalidSessionTokenException(String message) {
    super(message);
  }
}
