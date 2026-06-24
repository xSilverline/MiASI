package miasi.backend.authorization.infrastructure.out.security;

import miasi.backend.authorization.application.port.out.PasswordVerifierPort;
import org.mindrot.jbcrypt.BCrypt;

public class BcryptPasswordVerifier implements PasswordVerifierPort {
  @Override
  public boolean matches(String rawPassword, String passwordHash) {
    return BCrypt.checkpw(rawPassword, passwordHash);
  }
}
