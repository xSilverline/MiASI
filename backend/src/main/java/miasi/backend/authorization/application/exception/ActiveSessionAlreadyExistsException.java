package miasi.backend.authorization.application.exception;

public class ActiveSessionAlreadyExistsException extends AuthorizationException {
  public ActiveSessionAlreadyExistsException(String message) {
    super(message);
  }
}
