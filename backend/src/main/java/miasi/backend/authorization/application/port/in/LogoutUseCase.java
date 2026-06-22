package miasi.backend.authorization.application.port.in;

public interface LogoutUseCase {
  void logout(String token);
}
