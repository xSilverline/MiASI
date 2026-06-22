package miasi.backend.authorization.application.exception;

public class InvalidSessionTokenException extends AuthorizationException {
  public InvalidSessionTokenException(String message) {
    super(message);
  }
}
