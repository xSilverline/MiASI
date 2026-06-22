package miasi.backend.authorization.application.port.in;

public interface LoginUseCase {
  String login(String login, String password);
}
