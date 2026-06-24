package miasi.backend.api.config;

import miasi.backend.domains.authorization.Authorization;
import miasi.backend.domains.authorization.IUserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AuthConfig {
  @Bean
  public Authorization authorizationService(IUserRepository userRepository) {
    // adapter to domain
    return new Authorization(userRepository);
  }
}
