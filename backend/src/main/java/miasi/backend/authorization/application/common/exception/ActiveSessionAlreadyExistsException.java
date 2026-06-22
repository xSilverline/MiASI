package miasi.backend.authorization.application.common.exception;

public class ActiveSessionAlreadyExistsException extends AuthorizationException {
  public ActiveSessionAlreadyExistsException(String message) {
    super(message);
  }
}
