package miasi.backend.authorization.infrastructure.config;

import miasi.backend.authorization.application.port.out.PasswordVerifierPort;
import miasi.backend.authorization.application.port.out.UserRepositoryPort;
import miasi.backend.authorization.application.service.AuthorizationApplicationService;
import miasi.backend.authorization.infrastructure.out.security.BcryptPasswordVerifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AuthConfig {

  @Bean
  public PasswordVerifierPort passwordVerifierPort() {
    return new BcryptPasswordVerifier();
  }

  @Bean
  public AuthorizationApplicationService authorizationApplicationService(
      UserRepositoryPort userRepository, PasswordVerifierPort passwordVerifier) {
    return new AuthorizationApplicationService(userRepository, passwordVerifier);
  }
}
