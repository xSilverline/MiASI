package miasi.backend.config;

import miasi.backend.authorization.adapter.out.security.BcryptPasswordVerifier;
import miasi.backend.authorization.application.AuthorizationApplicationService;
import miasi.backend.authorization.application.port.out.PasswordVerifierPort;
import miasi.backend.domains.authorization.IUserRepository;
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
      IUserRepository userRepository, PasswordVerifierPort passwordVerifier) {
    return new AuthorizationApplicationService(userRepository, passwordVerifier);
  }
}
