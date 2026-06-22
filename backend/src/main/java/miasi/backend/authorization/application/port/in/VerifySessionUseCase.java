package miasi.backend.authorization.application.port.in;

public interface VerifySessionUseCase {
  boolean isAuthenticated(String token);
}
