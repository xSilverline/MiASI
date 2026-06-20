package miasi.backend.api.config;

import miasi.backend.database.JsonUserRepository;
import miasi.backend.domains.authorization.Authorization;
import miasi.backend.domains.authorization.IUserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AuthConfig {

  @Bean
  public IUserRepository userRepository() {
    // database adapter - json file reader
    return new JsonUserRepository();
  }

  @Bean
  public Authorization authorizationService(IUserRepository userRepository) {
    // adapter to domain
    return new Authorization(userRepository);
  }
}