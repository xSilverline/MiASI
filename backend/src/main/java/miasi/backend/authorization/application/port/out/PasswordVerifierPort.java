package miasi.backend.authorization.application.port.out;

public interface PasswordVerifierPort {
  boolean matches(String rawPassword, String passwordHash);
}
